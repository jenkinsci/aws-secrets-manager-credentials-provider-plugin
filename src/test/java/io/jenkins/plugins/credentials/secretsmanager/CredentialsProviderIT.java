package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.domains.Domain;

import io.jenkins.plugins.credentials.secretsmanager.util.Maps;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
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
        final Result foo = createStringSecret("supersecret");

        // When
        final List<String> credentialNames = lookupCredentialNames(StringCredentials.class);

        // Then
        assertThat(credentialNames).containsOnly(foo.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateDeletedCredentials() {
        // Given
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");
        // And
        final CreateSecretOperation.Result bar = createOtherStringSecret("supersecret");
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
        final CreateSecretOperation.Result foo = createStringSecret("supersecret");
        // And
        final CreateSecretOperation.Result bar = createOtherStringSecret("supersecret");

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
    public void shouldIgnoreUntaggedSecrets() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Collections.singletonMap("jenkins:credentials:type", "string");
        });
        // And
        final CreateSecretOperation.Result bar = createOtherSecret("supersecret", opts -> {
            opts.tags = Collections.emptyMap();
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
    public void shouldTolerateUnrelatedTags() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Maps.of(
                    "jenkins:credentials:type", "string",
                    "foo", "bar",
                    null, "baz",
                    "qux", null);
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
}
