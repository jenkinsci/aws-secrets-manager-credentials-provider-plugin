package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Secret Text credentials.
 */
public class StringCredentialsIT implements CredentialsTests {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var credentialList = jenkins.getCredentials().list(StringCredentials.class);

        // Then
        assertThat(credentialList)
                .containsOption(secret.getName(), secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var credential = jenkins.getCredentials().lookup(StringCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasId(secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveSecret() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var credential = jenkins.getCredentials().lookup(StringCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasSecret(SECRET);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final var secret = createStringSecret(SECRET);

        final var ours = jenkins.getCredentials().lookup(StringCredentials.class, secret.getName());

        final var theirs = new StringCredentialsImpl(null, "id", "description", Secret.fromString("secret"));

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var run = runPipeline("",
                "withCredentials([string(credentialsId: '" + secret.getName() + "', variable: 'VAR')]) {",
                "  echo \"Credential: $VAR\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: ****");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        VAR = credentials('" + secret.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $VAR}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretResult foo = createStringSecret(SECRET);
        final StringCredentials before = jenkins.getCredentials().lookup(StringCredentials.class, foo.getName());

        // When
        final StringCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getSecret()).as("Secret").isEqualTo(before.getSecret());
        });
    }

    private CreateSecretResult createStringSecret(String secretString) {
        final var tags = List.of(AwsTags.type(Type.string));

        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
