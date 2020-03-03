package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SecretSourceIT extends AbstractPluginIT {

    private static final String SECRET_STRING = "supersecret";
    public static final byte[] SECRET_BINARY = {0x01, 0x02, 0x03};

    private ConfigurationContext context;

    @Before
    public void refreshConfigurationContext() {
        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        context = new ConfigurationContext(registry);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotRevealMissingSecret() {
        // When
        final String secret = revealSecret("foo");

        // Then
        assertThat(secret).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/default.yml")
    public void shouldThrowExceptionWhenSecretsManagerUnavailable() {
        assertThatIOException()
                .isThrownBy(() -> revealSecret("foo"));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecret() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(SECRET_STRING, opts -> {});

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretWasSoftDeleted() {
        final CreateSecretOperation.Result foo = createSecret(SECRET_STRING, opts -> {});
        deleteSecret(foo.getName());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotRevealBinarySecret() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(SECRET_BINARY, opts -> {});

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldIgnoreFilters() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(SECRET_STRING, opts -> {
            opts.tags = Collections.singletonMap("wrong", "tag");
        });

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecretsOutsideCredentialsProvider() {
        final CreateSecretOperation.Result foo = createSecret(SECRET_STRING, opts -> {});

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as(SecretSource.class.getName()).isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredentials(StandardCredentials.class)).as(CredentialsProvider.class.getName()).isEmpty();
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecretsInsideCredentialsProvider() {
        final CreateSecretOperation.Result foo = createStringSecret(SECRET_STRING);

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as(SecretSource.class.getName()).isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredential(StringCredentials.class, foo.getName()).getSecret().getPlainText()).as(CredentialsProvider.class.getName()).isEqualTo(SECRET_STRING);
        });
    }

    private String revealSecret(String id) {
        return SecretSourceResolver.resolve(context, "${" + id + "}");
    }
}
