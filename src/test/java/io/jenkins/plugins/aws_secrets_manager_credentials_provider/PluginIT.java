package io.jenkins.plugins.aws_secrets_manager_credentials_provider;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.domains.Domain;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import hudson.security.ACL;
import hudson.util.Secret;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.ConfiguredWithCode;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.CreateSecretOperation;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.DeleteSecretOperation;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.CreateSecretOperation.Result;
import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.RestoreSecretOperation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@RunWith(LocalstackTestRunner.class)
public class PluginIT {

    private static final AWSSecretsManager CLIENT = TestUtils.getClientSecretsManager();
    private static final String FOO = "foo";
    private static final String BAR = "bar";

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private CredentialsStore store;

    private List<StringCredentials> lookupCredentials() {
        return CredentialsProvider.lookupCredentials(StringCredentials.class, r.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

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
        final List<StringCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportStringSecret() {
        // Given
        final Result foo = createSecret(FOO, "supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(foo.getValue())));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportPrivateKeySecret() {
        // Given
        final Result foo = createSecret(FOO, String.join("\n"
                , "-----BEGIN RSA PRIVATE KEY-----"
                , "MIIEowIBAAKCAQEAngWMYnda9vD2utvbAdgCOLVNanA/MW50er5ROW21it/eph1u"
                , "6RCuZ0CiuYUE5Eb8kOOQP7MTL3Ixyv9GW6hmMZwjyvcCamKj7cYuEHBYkn0X2Jgw"
                , "syPGUWZwITgSxgb/VfjRKbAtUdvXNFjHxknUlaVd+G6gQpN5Lv3//O/EglmVqf1d"
                , "CM2xAy9Ixk9roMSmBpgwC7lCsi1W9IGdLrjLAC96BrJkHX1EDQDdB8tWg8qLjZfr"
                , "L1ioddG/NDH8lOUetWX9SB5WF4xi/oBRNvSCwmBAa8v2DvhS/TEwcWAsReclRCNW"
                , "5eGAqhbb0Kl8E0hYJdFlEKYjQH3y5cZtqMAiuwIDAQABAoIBAGQK2TThoYpjRaFJ"
                , "XZ8ONWHXjpqLU8akykOHR/8WsO+qCdibG8OcFv4xkpPnXhBzzKSiHYnmgofwQQvm"
                , "j5GpzIEt/A8cUMAvkN8RL8qihcDAR5+Nwo83X+/a7bRqPqB2f6LbMvi0nAyOJPH0"
                , "Hw4vYdIX7qVAzF855GfW0QE+fueSdtgWviJM8gZHdhCqe/zqYm016zNaavap530r"
                , "tJ/+vhUW8WYqJqBW8+58laW5vTBusNsVjeL40yJF8X/XQQcdZ4XmthNcegx79oim"
                , "j9ELzX0ttchiwAe/trLxTkdWb4rEFz+U50iAOMUdS8T0brb5bxhqNM/ByiqQ28W9"
                , "2NJCVEkCgYEA0phCE9iKVWNZnvWX6+fHgr2NO2ShPexPeRfFxr0ugXGTQvyT0HnM"
                , "/Q//V+LduPMX8b2AsOzI0rQh+4bjohOZvKmGKiuPv3eSvqpi/r6208ZVTBjjFvBO"
                , "UQhMbPUyR6vO1ryFDwBMwMqQ06ldkXArhB+SG0dYnOKb/6g0nO2BVFUCgYEAwBeH"
                , "HGNGuxwum63UAaqyX6lRSpGGm6XSCBhzvHUPnVphgq7nnZOGl0z3U49jreCvuuEc"
                , "fA9YqxJjzoZy5870KOXY2kltlq/U/4Lrb0k75ag6ZVbi0oemACN6KCHtE+Zm2dac"
                , "rW8oKWpRTbsvMOYUvSjF0u8BCrestpRUF977Ks8CgYEAicbLFCjK9+ozq+eJKPFO"
                , "eZ6BU6YWR2je5Z5D6i3CyzT+3whXvECzd6yLpXfrDyEbPTB5jUacbB0lTmWFb3fb"
                , "UK6n89bkCKO2Ab9/XKJxAkPzcgGmME+vLRx8w5v29STWAW78rj/H9ymPbqqTaJ82"
                , "GQ5+jBI1Sw6GeNAW+8P2pLECgYAs/dXBimcosCMih4ZelZKN4WSO6KL0ldQp3UBO"
                , "ZcSwgFjSeRD60XD2wyoywiUAtt2yEcPQMu/7saT63HbRYKHDaoJuLkCiyLBE4G8w"
                , "c6C527tBvSYHVYpGAgk8mSWkQZTZdPDhlmV7vdEpOayF8X3uCDy9eQlvbzHe2cMQ"
                , "jEOb9QKBgG3jSxGfqN/sD8W9BhpVrybCXh2RvhxOBJAFx58wSWTkRcYSwpdyvm7x"
                , "wlMtcEdQgaSBeuBU3HPUdYE07bQNAlYO0p9MQnsLHzd2V9yiCX1Sq5iB6dQpHxyi"
                , "sDZLY2Mym1nUJWfE47GAcxFZtrVh9ojKcmgiHo8qPTkWjFGY7xe/"
                , "-----END RSA PRIVATE KEY-----"
        ));

        // When
        final List<StringCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(foo.getValue())));
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
        final List<StringCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(foo.getValue())));
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
        final List<StringCredentials> credentials = lookupCredentials();

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(foo.getValue())));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateRecentlyDeletedSecrets() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("foo", "supersecret");
        // And
        final CreateSecretOperation.Result bar = createSecret("bar", "supersecret");

        // When
        final List<StringCredentials> credentials = lookupCredentials();
        // And
        deleteSecret(bar.getName());

        // Then
        final StringCredentials fooCreds = credentials.stream().filter(c -> c.getId().equals("foo")).findFirst().orElseThrow(() -> new IllegalStateException("Needed the credential 'foo', but it did not exist"));
        final StringCredentials barCreds = credentials.stream().filter(c -> c.getId().equals("bar")).findFirst().orElseThrow(() -> new IllegalStateException("Needed the credential 'bar', but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString(foo.getValue()));
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

    private static Result createSecret(String name, String secretString) {
        final CreateSecretOperation create = new CreateSecretOperation(CLIENT);
        return create.run(name, secretString);
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
}
