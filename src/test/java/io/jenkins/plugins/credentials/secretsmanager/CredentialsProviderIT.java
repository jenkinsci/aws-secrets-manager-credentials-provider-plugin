package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.Crypto;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support CredentialsProvider usage. (This is the most direct way of using it.)
 */
public class CredentialsProviderIT extends AbstractPluginIT implements CredentialTypeTests {

    private static final Secret EMPTY_PASSPHRASE = Secret.fromString("");

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldStartEmpty() {
        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringCredentials() {
        // Given
        final Result foo = createSecret("supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportUsernamePasswordCredentials() {
        // Given
        final Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StandardUsernamePasswordCredentials> credentials =
                lookupCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "password")
                .containsOnly(tuple(foo.getName(), "joe", Secret.fromString("supersecret")));
    }

    /*
     * NOTE: This is not an officially supported feature. It may change without warning in future.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAllowUsernamePasswordCredentialsToBeUsedAsStringCredentials() {
        // Given
        final Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyCredentials() {
        // Given
        final String privateKey = Crypto.newPrivateKey();
        // And
        final Result foo = createSecret(privateKey, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<SSHUserPrivateKey> credentials = lookupCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(tuple(foo.getName(), "joe", privateKey, EMPTY_PASSPHRASE));
    }

    /*
     * NOTE: This is not an officially supported feature. It may change without warning in future.
     */
    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldAllowSshPrivateKeyCredentialsToBeUsedAsStringCredentials() {
        // Given
        final String privateKey = Crypto.newPrivateKey();
        // And
        final Result foo = createSecret(privateKey, opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(privateKey)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotTolerateInvalidSshPrivateKeyCredentials() {
        // Given
        final Result foo = createSecret("-----INVALID PRIVATE KEY", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", "joe");
        });

        // When
        final Optional<SSHUserPrivateKey> credentials =
                lookupCredentials(SSHUserPrivateKey.class).stream().findFirst();

        // Then
        assertSoftly(s -> {
            s.assertThat(credentials).isPresent();
            s.assertThat(credentials.get().getId()).as("ID").isEqualTo(foo.getName());
            s.assertThatThrownBy(() -> credentials.get().getPrivateKeys()).as("Private Keys").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateCredentials() {
        // Given
        final String alias = "test";
        final KeyPair keyPair = Crypto.newKeyPair();
        final Certificate cert = Crypto.newSelfSignedCertificate(keyPair);
        final char[] password = {};
        final KeyStore keyStore = Crypto.newKeyStore(password);
        try {
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, new Certificate[]{cert});
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        // And
        final Result foo = createSecret(Crypto.saveKeyStore(keyStore, password));

        // When
        final List<CertCreds> credentials = lookupCredentials(StandardCertificateCredentials.class)
                .stream()
                .map(cred -> new CertCreds(cred.getId(), keystoreToMap(cred.getKeyStore()), cred.getPassword()))
                .collect(Collectors.toList());

        // Then
        assertThat(credentials)
                .extracting("id", "password", "keyStore")
                .containsOnly(tuple(foo.getName(), EMPTY_PASSPHRASE, Collections.singletonMap(alias, Collections.singletonList(cert))));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotTolerateInvalidCertificateCredentials() {
        // Given
        final Result foo = createSecret(new byte[] {0x00, 0x01});

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

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldUseSecretNameAsCredentialName() {
        // Given
        final Result foo = createSecret("supersecret");

        // When
        final List<String> credentialNames = lookupCredentialNames(StringCredentials.class);

        // Then
        assertThat(credentialNames).containsOnly(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateDeletedCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret");
        // And
        final CreateSecretOperation.Result bar = createOtherSecret("supersecret");
        // And
        deleteSecret(bar.getName());

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateRecentlyDeletedCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret");
        // And
        final CreateSecretOperation.Result bar = createOtherSecret("supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);
        // And
        deleteSecret(bar.getName());

        // Then
        final StringCredentials fooCreds = credentials.stream().filter(c -> c.getId().equals(foo.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));
        final StringCredentials barCreds = credentials.stream().filter(c -> c.getId().equals(bar.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString("supersecret"));
            s.assertThatThrownBy(barCreds::getSecret).as("Bar").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateCredentialsWithNullTags() {
        // Given
        final Result foo = createSecret("supersecret", opts -> {
            opts.tags = null;
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateCredentialsWithNullTagKeys() {
        // Given
        final Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap(null, "foo");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateCredentialsWithNullTagValues() {
        // Given
        final Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:username", null);
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final StringCredentialsImpl credential = new StringCredentialsImpl(CredentialsScope.GLOBAL,"foo", "desc", Secret.fromString("password"));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store().updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store().addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store().removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

    private static Map<String, List<Certificate>> keystoreToMap(KeyStore keyStore) {
        final Map<String, List<Certificate>> ks = new HashMap<>();

        try {
            final Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                final String a = aliases.nextElement();
                final Certificate[] certificateChain = keyStore.getCertificateChain(a);
                final List<Certificate> certificateChainList = Arrays.asList(certificateChain);
                ks.put(a, certificateChainList);
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        return ks;
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
