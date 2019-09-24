package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support the Jenkinsfile 'withCredentials' step.
 */
public class WithCredentialsStepIT extends AbstractPluginIT implements CredentialTypeTests {

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret");

        // When
        final WorkflowRunResult result = runPipeline("",
                "withCredentials([string(credentialsId: '" + foo.getName() + "', variable: 'VAR')]) {",
                "  echo \"Credential: $VAR\"",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: ****");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportUsernamePasswordCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline("",
                "withCredentials([usernamePassword(credentialsId: '" + foo.getName() + "', usernameVariable: 'USR', passwordVariable: 'PSW')]) {",
                "  echo \"Credential: {username: $USR, password: $PSW}\"",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, password: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(Fixtures.SSH_PRIVATE_KEY, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final WorkflowRunResult result = runPipeline("",
                "node {",
                "  withCredentials([sshUserPrivateKey(credentialsId: '" + foo.getName() + "', keyFileVariable: 'KEYFILE', usernameVariable: 'USR')]) {",
                "    echo \"Credential: {username: $USR, keyFile: $KEYFILE}\"",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {username: ****, keyFile: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateCredentials() {
        // Given
        final KeyPair keyPair = Crypto.newKeyPair();
        final Certificate cert = Crypto.newSelfSignedCertificate(keyPair);
        final KeyStore keyStore = Crypto.newKeyStore(Fixtures.EMPTY_PASSWORD);
        try {
            keyStore.setKeyEntry("test", keyPair.getPrivate(), Fixtures.EMPTY_PASSWORD, new Certificate[]{cert});
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        // And
        final CreateSecretOperation.Result foo = createSecret(Crypto.saveKeyStore(keyStore, Fixtures.EMPTY_PASSWORD));

        // When
        final WorkflowRunResult result = runPipeline("",
                "node {",
                "  withCredentials([certificate(credentialsId: '" + foo.getName() + "', keystoreVariable: 'KEYSTORE')]) {",
                "    echo \"Credential: {keystore: $KEYSTORE}\"",
                "  }",
                "}");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.log).as("Log").contains("Credential: {keystore: ****}");
            s.assertThat(result.result).as("Result").isEqualTo(hudson.model.Result.SUCCESS);
        });
    }
}
