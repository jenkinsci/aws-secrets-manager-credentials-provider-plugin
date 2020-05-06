package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;

import static io.jenkins.plugins.credentials.secretsmanager.util.Crypto.keystoreToMap;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Certificate credentials.
 */
public class StandardCertificateCredentialsIT implements CredentialsTests {

    private static final String ALIAS = "test";
    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");
    private static final KeyPair KEY_PAIR = Crypto.newKeyPair();
    private static final char[] PASSWORD = new char[]{};
    private static final String CN = "CN=localhost";
    private static final Certificate CERT = Crypto.newSelfSignedCertificate(CN, KEY_PAIR);

    @Rule
    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();

    @Rule
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final ListBoxModel list = jenkins.getCredentials().list(StandardCertificateCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(CN, foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        final byte[] keystore = Crypto.save(Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT}), PASSWORD);
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(keystore);
        final StandardCertificateCredentials ours = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        final StandardCertificateCredentials theirs = new CertificateCredentialsImpl(null, "id", "description", "password", new CertificateCredentialsImpl.UploadedKeyStoreSource(SecretBytes.fromBytes(keystore)));

        assertThat(ours.getDescriptor().getIconClassName()).isEqualTo(theirs.getDescriptor().getIconClassName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final StandardCertificateCredentials credential = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        // Then
        assertThat(credential.getId()).isEqualTo(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveEmptyPassword() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final StandardCertificateCredentials credential = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        // Then
        assertThat(credential.getPassword()).isEqualTo(EMPTY_PASSPHRASE);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveKeystore() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final StandardCertificateCredentials credential = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        // Then
        assertThat(Crypto.keystoreToMap(credential.getKeyStore())).containsEntry(ALIAS, Collections.singletonList(CERT));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final WorkflowRun run = jenkins.getPipelines().run(Strings.m("",
                "node {",
                "  withCredentials([certificate(credentialsId: '" + foo.getName() + "', keystoreVariable: 'KEYSTORE')]) {",
                "    echo \"Credential: {keystore: $KEYSTORE}\"",
                "  }",
                "}"));

        // Then
        WorkflowRunAssert.assertThat(run)
                .hasResult(hudson.model.Result.SUCCESS)
                .hasLogContaining("Credential: {keystore: ****}");
    }

    @Ignore("Declarative Pipeline does not support certificate bindings")
    public void shouldSupportEnvironmentBinding() {

    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(Crypto.save(keyStore, PASSWORD));
        final StandardCertificateCredentials before = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        // When
        final StandardCertificateCredentials after = CredentialSnapshots.snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(before.getId());
            s.assertThat(after.getPassword()).as("Password").isEqualTo(before.getPassword());
            s.assertThat(keystoreToMap(after.getKeyStore())).as("KeyStore").containsEntry(ALIAS, Collections.singletonList(CERT));
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotTolerateMalformattedKeyStore() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createCertificateSecret(new byte[] {0x00, 0x01});

        // When
        final StandardCertificateCredentials credential = jenkins.getCredentials().lookup(StandardCertificateCredentials.class, foo.getName());

        // Then
        assertThatThrownBy(credential::getKeyStore).isInstanceOf(CredentialsUnavailableException.class);
    }
}
