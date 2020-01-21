package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support SSH private key credentials.
 */
public class SSHUserPrivateKeyIT extends AbstractPluginIT implements CredentialsTests {

    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");
    private static final String PRIVATE_KEY = Crypto.newPrivateKey();
    private static final String USERNAME = "joe";

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveName() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final ListBoxModel list = listCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(USERNAME, foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAppearInCredentialsProvider() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final List<SSHUserPrivateKey> credentials = lookupCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(tuple(foo.getName(), USERNAME, PRIVATE_KEY, EMPTY_PASSPHRASE));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "node {",
                "  withCredentials([sshUserPrivateKey(credentialsId: '" + foo.getName() + "', keyFileVariable: 'KEYFILE', usernameVariable: 'USERNAME')]) {",
                "    echo \"Credential: {username: $USERNAME, keyFile: $KEYFILE}\"",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, keyFile: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final WorkflowRunResult result = runPipeline(Strings.m("",
                "pipeline {",
                "  agent any",
                "  stages {",
                "    stage('Example') {",
                "      environment {",
                "        FOO = credentials('" + foo.getName() + "')",
                "      }",
                "      steps {",
                "        echo \"{variable: $FOO, username: $FOO_USR}\"",
                "      }",
                "    }",
                "  }",
                "}"));

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("{variable: ****, username: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);
        // And
        final SSHUserPrivateKey before = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // When
        final SSHUserPrivateKey after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(foo.getName(), USERNAME, PRIVATE_KEY, EMPTY_PASSPHRASE);
    }
}
