package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * The plugin should support SSH private key credentials.
 */
public class SSHUserPrivateKeyIT implements CredentialsTests {

    private static final String PRIVATE_KEY = Crypto.newPrivateKey();
    private static final String USERNAME = "joe";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var credentialList = jenkins.getCredentials().list(SSHUserPrivateKey.class);

        // Then
        assertThat(credentialList)
                .containsOption(secret.getName(), secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var credential = lookup(SSHUserPrivateKey.class, secret.getName());

        // Then
        assertThat(credential)
                .hasId(secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var credential = lookup(SSHUserPrivateKey.class, secret.getName());

        // Then
        assertThat(credential)
                .hasUsername(USERNAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePrivateKey() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var credential = lookup(SSHUserPrivateKey.class, secret.getName());

        // Then
        assertThat(credential)
                .hasPrivateKeys(List.of(PRIVATE_KEY));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveEmptyPassphrase() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var credential = lookup(SSHUserPrivateKey.class, secret.getName());

        // Then
        assertThat(credential)
                .doesNotHavePassphrase();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        final var ours = lookup(SSHUserPrivateKey.class, secret.getName());

        final var theirs = new BasicSSHUserPrivateKey(null, "id", "username", null, "passphrase", "description");

        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var run = runPipeline("",
                "node {",
                "  withCredentials([sshUserPrivateKey(credentialsId: '" + secret.getName() + "', keyFileVariable: 'KEYFILE', usernameVariable: 'USERNAME')]) {",
                "    echo \"Credential: {username: $USERNAME, keyFile: $KEYFILE}\"",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, keyFile: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final var secret = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final var run = runPipeline("",
                "pipeline {",
                "  agent any",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + secret.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR}\"",
                "      }",
                "    }",
                "  }",
                "}");

        // Then
        assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****, username: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretResult foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);
        final SSHUserPrivateKey before = lookup(SSHUserPrivateKey.class, foo.getName());

        // When
        final SSHUserPrivateKey after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasUsername(before.getUsername())
                .hasPassphrase(before.getPassphrase())
                .hasPrivateKeys(before.getPrivateKeys())
                .hasId(before.getId());
    }

    private CreateSecretResult createSshUserPrivateKeySecret(String username, String privateKey) {
        final var tags = List.of(
                AwsTags.type("sshUserPrivateKey"),
                AwsTags.username(username));

        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(privateKey)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }

    private WorkflowRun runPipeline(String... pipeline) {
        return jenkins.getPipelines().run(Strings.m(pipeline));
    }
}
