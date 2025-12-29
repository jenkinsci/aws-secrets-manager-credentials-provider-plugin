package io.jenkins.plugins.credentials.secretsmanager;

import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * The plugin should support Username With Password credentials.
 */
public class StandardUsernamePasswordCredentialsIT implements CredentialsTests {

    private static final String USERNAME = "joe";
    private static final String PASSWORD = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var credentialList = jenkins.getCredentials().list(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentialList)
                .containsOption(secret.name(), secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePassword() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasPassword(PASSWORD);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasUsername(USERNAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasId(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var run = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + secret.name() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + secret.name() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR, password: $FOO_PSW}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****, username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final var foo = createUsernamePasswordSecret(USERNAME, PASSWORD);
        final StandardUsernamePasswordCredentials before = jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, foo.name());

        // When
        final StandardUsernamePasswordCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasUsername(before.getUsername())
                .hasPassword(before.getPassword())
                .hasId(before.getId());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD);

        final var ours = jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.name());

        try {
            final var theirs = new UsernamePasswordCredentialsImpl(null, "id", "description", "username", "password");

            assertThat(ours)
                    .hasSameDescriptorIconAs(theirs);
        } catch (Descriptor.FormException e) {
            throw new RuntimeException(e);
        }
    }

    private CreateSecretResponse createUsernamePasswordSecret(String username, String password) {
        final var tags = List.of(
                AwsTags.type(Type.usernamePassword),
                AwsTags.username(username));

        return secretsManager.getClient().createSecret((b) -> {
            b.name(CredentialNames.random());
            b.secretString(password);
            b.tags(tags);
        });
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
