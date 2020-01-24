package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Test;

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
    public void shouldSupportListView() {
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
    public void shouldHaveId() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final SSHUserPrivateKey credential = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveUsername() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final SSHUserPrivateKey credential = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // Then
        assertThat(credential.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePrivateKey() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final SSHUserPrivateKey credential = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // Then
        assertThat(credential.getPrivateKeys()).containsOnly(PRIVATE_KEY);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveEmptyPassphrase() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final SSHUserPrivateKey credential = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // Then
        assertThat(credential.getPassphrase()).isEqualTo(EMPTY_PASSPHRASE);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);
        final SSHUserPrivateKey ours = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        final BasicSSHUserPrivateKey theirs = new BasicSSHUserPrivateKey(null, "id", "username", null, "passphrase", "description");

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final WorkflowRun run = runPipeline(Strings.m("",
                "node {",
                "  withCredentials([sshUserPrivateKey(credentialsId: '" + foo.getName() + "', keyFileVariable: 'KEYFILE', usernameVariable: 'USERNAME')]) {",
                "    echo \"Credential: {username: $USERNAME, keyFile: $KEYFILE}\"",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {username: ****, keyFile: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportEnvironmentBinding() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);

        // When
        final WorkflowRun run = runPipeline(Strings.m("",
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
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("{variable: ****, username: ****}");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final CreateSecretOperation.Result foo = createSshUserPrivateKeySecret(USERNAME, PRIVATE_KEY);
        final SSHUserPrivateKey before = lookupCredential(SSHUserPrivateKey.class, foo.getName());

        // When
        final SSHUserPrivateKey after = snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getUsername()).as("Username").isEqualTo(before.getUsername());
            s.assertThat(after.getPassphrase()).as("Passphrase").isEqualTo(before.getPassphrase());
            s.assertThat(after.getPrivateKeys()).as("Private Key").isEqualTo(before.getPrivateKeys());
        });
    }
}
