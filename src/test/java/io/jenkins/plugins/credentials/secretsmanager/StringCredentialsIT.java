package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Secret Text credentials.
 */
public class StringCredentialsIT extends AbstractPluginIT implements CredentialsTests {

    private static final String SECRET = "supersecret";

    @Rule
    public AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final ListBoxModel list = listCredentials(StringCredentials.class);

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
        final StringCredentials credential = lookupCredential(StringCredentials.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveSecret() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final StringCredentials credential = lookupCredential(StringCredentials.class, foo.getName());

        // Then
        assertThat(credential.getSecret()).isEqualTo(Secret.fromString(SECRET));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final StringCredentials ours = lookupCredential(StringCredentials.class, foo.getName());

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
        final WorkflowRun run = runPipeline(Strings.m("",
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
        final WorkflowRun run = runPipeline(Strings.m("",
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
        final StringCredentials before = lookupCredential(StringCredentials.class, foo.getName());

        // When
        final StringCredentials after = snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getSecret()).as("Secret").isEqualTo(before.getSecret());
        });
    }
}
