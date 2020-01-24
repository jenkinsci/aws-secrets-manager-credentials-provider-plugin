package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;

import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.WorkflowRunAssert;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Ignore;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.Strings;

import static io.jenkins.plugins.credentials.secretsmanager.util.Crypto.keystoreToMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support Certificate credentials.
 */
public class StandardCertificateCredentialsIT extends AbstractPluginIT implements CredentialsTests {

    private static final String ALIAS = "test";
    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");
    private static final KeyPair KEY_PAIR = Crypto.newKeyPair();
    private static final char[] PASSWORD = new char[]{};
    private static final String CN = "CN=localhost";
    private static final Certificate CERT = Crypto.newSelfSignedCertificate(CN, KEY_PAIR);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveName() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        // And
        final CreateSecretOperation.Result foo = createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final ListBoxModel list = listCredentials(StandardCertificateCredentials.class);

        // Then
        assertThat(list)
                .extracting("name", "value")
                .containsOnly(tuple(CN, foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveIcon() {
        final byte[] keystore = Crypto.save(Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT}), PASSWORD);
        final CreateSecretOperation.Result foo = createCertificateSecret(keystore);
        final StandardCertificateCredentials ours = lookupCredential(StandardCertificateCredentials.class, foo.getName());

        final StandardCertificateCredentials theirs = new CertificateCredentialsImpl(null, "id", "description", "password", new CertificateCredentialsImpl.UploadedKeyStoreSource(SecretBytes.fromBytes(keystore)));

        assertThat(ours.getDescriptor().getIconClassName())
                .isEqualTo(theirs.getDescriptor().getIconClassName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAppearInCredentialsProvider() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        // And
        final CreateSecretOperation.Result foo = createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final List<CertCreds> credentials = lookupCredentials(StandardCertificateCredentials.class)
                .stream()
                .map(cred -> new CertCreds(cred.getId(), Crypto.keystoreToMap(cred.getKeyStore()), cred.getPassword()))
                .collect(Collectors.toList());

        // Then
        assertThat(credentials)
                .extracting("id", "password", "keyStore")
                .containsOnly(tuple(foo.getName(), EMPTY_PASSPHRASE, Collections.singletonMap(ALIAS, Collections.singletonList(CERT))));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportWithCredentialsBinding() {
        // Given
        final KeyStore keyStore = Crypto.singletonKeyStore(ALIAS, KEY_PAIR.getPrivate(), PASSWORD, new Certificate[]{CERT});
        // And
        final CreateSecretOperation.Result foo = createCertificateSecret(Crypto.save(keyStore, PASSWORD));

        // When
        final WorkflowRun run = runPipeline(Strings.m("",
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
        // And
        final CreateSecretOperation.Result foo = createCertificateSecret(Crypto.save(keyStore, PASSWORD));
        // And
        final StandardCertificateCredentials before = lookupCredential(StandardCertificateCredentials.class, foo.getName());

        // When
        final StandardCertificateCredentials after = snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(foo.getName());
            s.assertThat(after.getPassword()).as("Password").isEqualTo(EMPTY_PASSPHRASE);
            s.assertThat(keystoreToMap(after.getKeyStore())).as("KeyStore").containsEntry(ALIAS, Collections.singletonList(CERT));
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotTolerateMalformattedKeyStore() {
        // Given
        final CreateSecretOperation.Result foo = createCertificateSecret(new byte[] {0x00, 0x01});

        // When
        final Optional<StandardCertificateCredentials> credentials =
                lookupCredentials(StandardCertificateCredentials.class).stream().findFirst();

        // Then
        assertSoftly(s -> {
            s.assertThat(credentials).isPresent();
            s.assertThat(credentials.get().getId()).as("ID").isEqualTo(foo.getName());
            s.assertThatThrownBy(() -> credentials.get().getKeyStore()).as("KeyStore").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    /*
     * KeyStore does not have a proper equals() implementation so we have to work around this.
     */
    private static class CertCreds {
        final String id;
        final Map<String, List<Certificate>> keyStore;
        final Secret password;

        CertCreds(String id, Map<String, List<Certificate>> keyStore, Secret password) {
            this.id = id;
            this.keyStore = keyStore;
            this.password = password;
        }
    }
}
