package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
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
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var credentialList = jenkins.getCredentials().list(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentialList)
                .containsOption(secret.getName(), secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListViewUnmasked() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, false);

        // When
        final var credentialList = jenkins.getCredentials().list(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentialList)
                .containsOption(USERNAME + "/******", secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePassword() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasPassword(PASSWORD);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasUsername(USERNAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var credential =
                jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.getName());

        // Then
        assertThat(credential)
                .hasId(secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var run = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + secret.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBindingUnmasked() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, false);

        // When
        final var run = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + secret.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: " + USERNAME + ", password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + secret.getName() + "')",
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
    public void shouldSupportEnvironmentBindingUnmasked() {
        // Given
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, false);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent none",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + secret.getName() + "')",
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
                .hasLogContaining("{variable: ****, username: " + USERNAME + ", password: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretResult foo = createUsernamePasswordSecret(USERNAME, PASSWORD, true);
        final StandardUsernamePasswordCredentials before = jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, foo.getName());

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
        final var secret = createUsernamePasswordSecret(USERNAME, PASSWORD, true);

        final var ours = jenkins.getCredentials().lookup(StandardUsernamePasswordCredentials.class, secret.getName());

        final var theirs = new UsernamePasswordCredentialsImpl(null, "id", "description", "username", "password");

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    private CreateSecretResult createUsernamePasswordSecret(String username, String password, Boolean maskUsername) {
        final var tags = List.of(
                AwsTags.type(Type.usernamePassword),
                AwsTags.username(username),
                AwsTags.maskUsername(String.valueOf(maskUsername)));

        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(password)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
