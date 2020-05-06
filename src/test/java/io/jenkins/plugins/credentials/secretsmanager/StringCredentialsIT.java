package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Secret Text credentials.
 */
public class StringCredentialsIT implements CredentialsTests {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(Rules.awsAccessKey("fake", "fake"))
            .around(jenkins)
            .around(secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final ListBoxModel list = jenkins.getCredentials().list(StringCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(foo.getName(), foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final StringCredentials credential = jenkins.getCredentials().lookup(StringCredentials.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveSecret() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final StringCredentials credential = jenkins.getCredentials().lookup(StringCredentials.class, foo.getName());

        // Then
        assertThat(credential.getSecret()).isEqualTo(Secret.fromString(SECRET));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final StringCredentials ours = jenkins.getCredentials().lookup(StringCredentials.class, foo.getName());

        final StringCredentials theirs = new StringCredentialsImpl(null, "id", "description", Secret.fromString("secret"));

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final WorkflowRun run = jenkins.getPipelines().run(Strings.m("",
                "withCredentials([string(credentialsId: '" + foo.getName() + "', variable: 'VAR')]) {",
                "  echo \"Credential: $VAR\"",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: ****");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final WorkflowRun run = jenkins.getPipelines().run(Strings.m("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final StringCredentials before = jenkins.getCredentials().lookup(StringCredentials.class, foo.getName());

        // When
        final StringCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getSecret()).as("Secret").isEqualTo(before.getSecret());
        });
    }
}
