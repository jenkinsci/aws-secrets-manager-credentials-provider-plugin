package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.domains.Domain;

import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.Maps;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support CredentialsProvider usage to list available credentials.
 */
public class CredentialsProviderIT extends AbstractPluginIT {

    private static final String SECRET = "supersecret";

    @Rule
    public AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    private CredentialsStore store;

    @Before
    public void setupStore() {
        store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();
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
    @ConfiguredWithCode(value = "/default.yml")
    public void shouldFailGracefullyWhenSecretsManagerUnavailable() {
        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldUseSecretNameAsCredentialName() {
        // Given
        final Result foo = secretsManager.createStringSecret(SECRET);

        // When
        final List<String> credentialNames = lookupCredentialNames(StringCredentials.class);

        // Then
        assertThat(credentialNames).containsOnly(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateDeletedCredentials() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final CreateSecretOperation.Result bar = secretsManager.createOtherStringSecret(SECRET);

        // When
        secretsManager.deleteSecret(bar.getName());
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateRecentlyDeletedCredentials() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final CreateSecretOperation.Result bar = secretsManager.createOtherStringSecret(SECRET);

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);
        secretsManager.deleteSecret(bar.getName());

        // Then
        final StringCredentials fooCreds = credentials.stream().filter(c -> c.getId().equals(foo.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));
        final StringCredentials barCreds = credentials.stream().filter(c -> c.getId().equals(bar.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString(SECRET));
            s.assertThatThrownBy(barCreds::getSecret).as("Bar").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldIgnoreUntaggedSecrets() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET);
        final CreateSecretOperation.Result bar = secretsManager.createOtherSecret(SECRET, opts -> {
            opts.tags = Collections.emptyMap();
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateUnrelatedTags() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET, opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.string,
                    "foo", "bar",
                    null, "baz",
                    "qux", null);
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final StringCredentialsImpl credential = new StringCredentialsImpl(CredentialsScope.GLOBAL,"foo", "desc", Secret.fromString(SECRET));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }
}
