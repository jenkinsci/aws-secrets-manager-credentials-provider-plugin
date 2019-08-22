package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import hudson.security.ACL;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.DeleteSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation.Result;
import io.jenkins.plugins.credentials.secretsmanager.util.RestoreSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PluginIT {

    private static final AWSSecretsManager CLIENT = TestUtils.getClientSecretsManager();
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    public static final char[] EMPTY_PASSWORD = {};

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private CredentialsStore store;

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    @Before
    public void setup() {
        store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();

        for (String secretId: Arrays.asList(FOO, BAR)) {
            restoreSecret(secretId);
            deleteSecret(secretId, opts -> opts.force = true);
        }
    }

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
    public void shouldSupportStringSecret() {
        // Given
        final Result foo = createSecret(FOO, "supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportUsernamePasswordSecret() {
        // Given
        final Result foo = createSecret(FOO, "supersecret", opts -> {
            opts.tags = Collections.singletonMap("username", "joe");
        });

        // When
        final List<StandardUsernamePasswordCredentials> credentials =
                lookupCredentials(StandardUsernamePasswordCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "password")
                .containsOnly(tuple(foo.getName(), "joe", Secret.fromString("supersecret")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyPkcs1Secret() {
        // Given
        final String privateKey =
                "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXQIBAAKBgQDbyoNEw32kJTN3/Bgnhr2GDlJ74YbwaFXMLC2V2j98+384NYra\n" +
                "/mDOsBBU9eBLH7dKMGLvaTFGzUliIAASrJSoWAxJGaAKwHPnkG44gpf+wKQNybXn\n" +
                "54vsNqDRFuz0BzDcD3YKEtqdT2eK/wJn80uarnQl4QcPfZx8/ELuVxo3uwIDAQAB\n" +
                "AoGBAMCc26byTvP/qfg3U4+oFAUcHgr0XIXoWXAhMx3E8qh72kSPH43FKV9Yiid6\n" +
                "hkIvnDgG6Vz36bgrhWjZtFapKWgyFZ2sRbrcU5+4Ks3+96V7abSF71KkWl/WWvMQ\n" +
                "61/Q5lC6ZRFiLVdI4JJhAgdu0qrDCTxHMYtkxzYAvNGE4jX5AkEA9rsBVdyNGi85\n" +
                "VRXHxs/0EG/56okBaMuVpd9l4noPcWRlrWwCYU/paExZimYyy5+5hp4TcTmQLw2Q\n" +
                "jRo3cj5SHwJBAOQMaR6jfsgKIydqC9ZqcIH3YKI7AV/e3+3qXL3GHPcVqJVIMGSF\n" +
                "DHqFW42B2WjdSpG4k4LisIBblsuHpXQg/uUCQAE2VgFX/hF83ek/HCYr62URR8cR\n" +
                "OUKMjYWtHVEJjH3gImfBuhlETT9H8MCvU9yQQlcY+7t4ru6sQGORF2imSb0CQQCb\n" +
                "5agPG/HVyqhRj3tcLxOOpZBYF0JPScuHl4mi6kZu202OD/WVIidvsq7tw/DecTlC\n" +
                "+Q1Okq3accJajPacttnJAkAhM+1WigW6myaRQYkr648Vo47RFbtAJzn+RTY46sEn\n" +
                "srzbfLspVubVfrJ/kh4LIEwPapfxPb7QQeK0guUABL/B\n" +
                "-----END RSA PRIVATE KEY-----";

        final Result foo = createSecret(FOO, privateKey, opts -> {
            opts.tags = Collections.singletonMap("username", "joe");
        });

        // When
        final List<SSHUserPrivateKey> credentials = lookupCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(tuple(foo.getName(), "joe", privateKey, Secret.fromString("")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSshPrivateKeyPkcs8Secret() {
        // Given
        final String privateKey =
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANvKg0TDfaQlM3f8\n" +
                "GCeGvYYOUnvhhvBoVcwsLZXaP3z7fzg1itr+YM6wEFT14Esft0owYu9pMUbNSWIg\n" +
                "ABKslKhYDEkZoArAc+eQbjiCl/7ApA3Jtefni+w2oNEW7PQHMNwPdgoS2p1PZ4r/\n" +
                "AmfzS5qudCXhBw99nHz8Qu5XGje7AgMBAAECgYEAwJzbpvJO8/+p+DdTj6gUBRwe\n" +
                "CvRchehZcCEzHcTyqHvaRI8fjcUpX1iKJ3qGQi+cOAbpXPfpuCuFaNm0VqkpaDIV\n" +
                "naxFutxTn7gqzf73pXtptIXvUqRaX9Za8xDrX9DmULplEWItV0jgkmECB27SqsMJ\n" +
                "PEcxi2THNgC80YTiNfkCQQD2uwFV3I0aLzlVFcfGz/QQb/nqiQFoy5Wl32Xieg9x\n" +
                "ZGWtbAJhT+loTFmKZjLLn7mGnhNxOZAvDZCNGjdyPlIfAkEA5AxpHqN+yAojJ2oL\n" +
                "1mpwgfdgojsBX97f7epcvcYc9xWolUgwZIUMeoVbjYHZaN1KkbiTguKwgFuWy4el\n" +
                "dCD+5QJAATZWAVf+EXzd6T8cJivrZRFHxxE5QoyNha0dUQmMfeAiZ8G6GURNP0fw\n" +
                "wK9T3JBCVxj7u3iu7qxAY5EXaKZJvQJBAJvlqA8b8dXKqFGPe1wvE46lkFgXQk9J\n" +
                "y4eXiaLqRm7bTY4P9ZUiJ2+yru3D8N5xOUL5DU6SrdpxwlqM9py22ckCQCEz7VaK\n" +
                "BbqbJpFBiSvrjxWjjtEVu0AnOf5FNjjqwSeyvNt8uylW5tV+sn+SHgsgTA9ql/E9\n" +
                "vtBB4rSC5QAEv8E=\n" +
                "-----END PRIVATE KEY-----";

        final Result foo = createSecret(FOO, privateKey, opts -> {
            opts.tags = Collections.singletonMap("username", "joe");
        });

        // When
        final List<SSHUserPrivateKey> credentials = lookupCredentials(SSHUserPrivateKey.class);

        // Then
        assertThat(credentials)
                .extracting("id", "username", "privateKey", "passphrase")
                .containsOnly(tuple(foo.getName(), "joe", privateKey, Secret.fromString("")));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportCertificateSecret() throws KeyStoreException {
        // Given
        final String alias = "test";
        final KeyPair keyPair = newKeyPair();
        final Certificate cert = newSelfSignedCertificate(keyPair);
        final KeyStore keyStore = newKeyStore();
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), new char[] {}, new Certificate[] {cert});
        // And
        final Result foo = createSecret(FOO, saveKeyStore(keyStore));

        // When
        final List<CertCreds> credentials = lookupCredentials(StandardCertificateCredentials.class)
                .stream()
                .map(cred -> new CertCreds(cred.getId(), keystoreToMap(cred.getKeyStore()), cred.getPassword()))
                .collect(Collectors.toList());

        // Then
        assertThat(credentials)
                .extracting("id", "password", "keyStore")
                .containsOnly(tuple(foo.getName(), Secret.fromString(""), Collections.singletonMap(alias, Collections.singletonList(cert))));
    }

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldFilterByTag() {
        // Given
        final Result foo = createSecret(FOO, "supersecret", opts -> {
            opts.tags = Collections.singletonMap("product", "roadrunner");
        });
        // And
        final Result bar = createSecret(BAR, "supersecret", opts -> {
            opts.tags = Collections.singletonMap("product", "coyote");
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
    public void shouldFilterByDeletionStatus() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("foo", "supersecret");
        // And
        final CreateSecretOperation.Result bar = createSecret("bar", "supersecret");
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
    public void shouldTolerateRecentlyDeletedSecrets() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("foo", "supersecret");
        // And
        final CreateSecretOperation.Result bar = createSecret("bar", "supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);
        // And
        deleteSecret(bar.getName());

        // Then
        final StringCredentials fooCreds = credentials.stream().filter(c -> c.getId().equals("foo")).findFirst().orElseThrow(() -> new IllegalStateException("Needed the credential 'foo', but it did not exist"));
        final StringCredentials barCreds = credentials.stream().filter(c -> c.getId().equals("bar")).findFirst().orElseThrow(() -> new IllegalStateException("Needed the credential 'bar', but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString("supersecret"));
            s.assertThatThrownBy(barCreds::getSecret).as("Bar").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final StringCredentialsImpl credential = new StringCredentialsImpl(CredentialsScope.GLOBAL, FOO, "desc", Secret.fromString("password"));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, FOO, "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, FOO, "desc", Secret.fromString("password"))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

    private <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    private static byte[] saveKeyStore(KeyStore keyStore) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            keyStore.store(baos, new char[] {});
            return baos.toByteArray();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore newKeyStore() {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, EMPTY_PASSWORD);
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyPair newKeyPair() {
        final KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(512);
        return keyPairGenerator.generateKeyPair();
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

    private static X509Certificate newSelfSignedCertificate(KeyPair keyPair) {
        try {
            final X500Name cn = new X500Name("CN=localhost");
            final X509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(
                    cn,
                    BigInteger.valueOf(Math.abs(new SecureRandom().nextInt())),
                    new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30)), // Not after 99 days from now
                    new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 99)), // Subject
                    cn,
                    keyPair.getPublic()
            );
            final X509CertificateHolder holder = b.build(new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate()));
            return new JcaX509CertificateConverter().getCertificate(holder);
        } catch (CertificateException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Result createSecret(String name, String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        return create.run(name, secretString);
    }

    private static Result createSecret(String name, byte[] secretBinary) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        return create.run(name, secretBinary);
    }

    private static Result createSecret(String name, String secretString, Consumer<CreateSecretOperation.Opts> opts) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        return create.run(name, secretString, opts);
    }

    private static void deleteSecret(String secretId) {
        final DeleteSecretOperation delete = new DeleteSecretOperation(CLIENT);
        delete.run(secretId);
    }

    private static void deleteSecret(String secretId, Consumer<DeleteSecretOperation.Opts> opts) {
        final DeleteSecretOperation delete = new DeleteSecretOperation(CLIENT);
        delete.run(secretId, opts);
    }

    private static void restoreSecret(String secretId) {
        final RestoreSecretOperation restore = new RestoreSecretOperation(CLIENT);
        restore.run(secretId);
    }

    /*
     * KeyStore does not have a proper equals() implementation so we have to work around this.
     */
    private static class CertCreds {
        final String id;
        final Map<String, List<Certificate>> keyStore;
        final Secret password;

        private CertCreds(String id, Map<String, List<Certificate>> keyStore, Secret password) {
            this.id = id;
            this.keyStore = keyStore;
            this.password = password;
        }
    }
}
