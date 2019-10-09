package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import org.apache.commons.lang3.SerializationUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;

import static io.jenkins.plugins.credentials.secretsmanager.util.Crypto.keystoreToMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should provide credentials that support snapshots and serialization.
 */
public class SnapshotIT extends AbstractPluginIT implements CredentialsTests {

    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret");
        // And
        final StringCredentials before = lookupCredential(AwsCredentials.class, foo.getName());

        // When
        final StringCredentials after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "secret")
                .containsOnly(foo.getName(), Secret.fromString("supersecret"));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportUsernamePasswordCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });
        // And
        final StandardUsernamePasswordCredentials before = lookupCredential(AwsCredentials.class, foo.getName());

        // When
        final StandardUsernamePasswordCredentials after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "password")
                .containsOnly(foo.getName(), "joe", Secret.fromString("supersecret"));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyCredentials() {
        // Given
        final String privateKey = Crypto.newPrivateKey();
        // And
        final CreateSecretOperation.Result foo = createSecret(privateKey, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });
        // And
        final SSHUserPrivateKey before = lookupCredential(AwsCredentials.class, foo.getName());

        // When
        final SSHUserPrivateKey after = snapshot(before);

        // Then
        assertThat(after)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(foo.getName(), "joe", privateKey, EMPTY_PASSPHRASE);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateCredentials() {
        // Given
        final KeyPair keyPair = Crypto.newKeyPair();
        final Certificate cert = Crypto.newSelfSignedCertificate(keyPair);
        final char[] password = {};
        final String alias = "test";
        final KeyStore keyStore = Crypto.singletonKeyStore(alias, keyPair.getPrivate(), password, new Certificate[]{cert});
        // And
        final CreateSecretOperation.Result foo = createSecret(Crypto.saveKeyStore(keyStore, password));
        // And
        final StandardCertificateCredentials before = lookupCredential(AwsCredentials.class, foo.getName());

        // When
        final StandardCertificateCredentials after = snapshot(before);

        // Then
        assertSoftly(s -> {
            s.assertThat(after.getId()).as("ID").isEqualTo(foo.getName());
            s.assertThat(after.getPassword()).as("Password").isEqualTo(EMPTY_PASSPHRASE);
            s.assertThat(keystoreToMap(after.getKeyStore())).as("KeyStore").containsEntry(alias, Collections.singletonList(cert));
        });
    }

    private <C extends StandardCredentials> C lookupCredential(Class<C> type, String id) {
        return lookupCredentials(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected a credential but none was present"));
    }

    private static <C extends StandardCredentials> C snapshot(C credentials) {
        return SerializationUtils.deserialize(SerializationUtils.serialize(CredentialsProvider.snapshot(credentials)));
    }
}
