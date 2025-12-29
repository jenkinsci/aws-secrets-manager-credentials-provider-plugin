package io.jenkins.plugins.credentials.secretsmanager;

import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support CredentialsProvider usage to list available credentials.
 */
public class CredentialsProviderIT {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldStartEmpty() {
        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/default.yml")
    public void shouldFailGracefullyWhenSecretsManagerUnavailable() {
        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldUseSecretNameAsCredentialName() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var credentialNames = jenkins.getCredentials().list(StringCredentials.class);

        // Then
        assertThat(credentialNames)
                .extracting("name")
                .containsOnly(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateDeletedCredentials() {
        // Given
        final var foo = createStringSecret(SECRET);
        final var bar = createStringSecret(SECRET);

        // When
        deleteSecret(bar.name());
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.name(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateRecentlyDeletedCredentials() {
        // Given
        final var foo = createStringSecret(SECRET);
        final var bar = createStringSecret(SECRET);

        // When
        final var credentials = lookup(StringCredentials.class);
        deleteSecret(bar.name());

        // Then
        final var fooCreds = credentials.stream().filter(c -> c.getId().equals(foo.name())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));
        final var barCreds = credentials.stream().filter(c -> c.getId().equals(bar.name())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString(SECRET));
            s.assertThatThrownBy(barCreds::getSecret).as("Bar").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldIgnoreUntaggedSecrets() {
        // Given
        final var foo = createStringSecret(SECRET);
        final var bar = createSecret(SECRET, List.of());

        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.name(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateUnrelatedTags() {
        // Given
        final var tags = List.of(
                AwsTags.type(Type.string),
                AwsTags.tag("foo", "bar"),
                AwsTags.tag("qux", null));

        final var secret = createSecret(SECRET, tags);

        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(secret.name(), Secret.fromString(SECRET)));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final var credential = new StringCredentialsImpl(CredentialsScope.GLOBAL,"foo", "desc", Secret.fromString(SECRET));

        final var store = jenkins.getCredentials().lookupStore(AwsCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        final var store = jenkins.getCredentials().lookupStore(AwsCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        final var store = jenkins.getCredentials().lookupStore(AwsCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

    private <C extends StandardCredentials> List<C> lookup(Class<C> type) {
        return jenkins.getCredentials().lookup(type);
    }

    private void deleteSecret(String secretId) {
        secretsManager.getClient().deleteSecret(secret -> {
            secret.secretId(secretId);
        });
    }

    private CreateSecretResponse createStringSecret(String secretString) {
        final var tags = List.of(AwsTags.type(Type.string));

        return createSecret(secretString, tags);
    }

    private CreateSecretResponse createSecret(String secretString, List<Tag> tags) {
        return secretsManager.getClient().createSecret(secret -> {
            secret.name(CredentialNames.random());
            secret.secretString(secretString);
            secret.tags(tags);
        });
    }
}
