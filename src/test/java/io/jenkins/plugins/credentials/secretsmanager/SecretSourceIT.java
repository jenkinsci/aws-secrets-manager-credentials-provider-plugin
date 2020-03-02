package io.jenkins.plugins.credentials.secretsmanager;

import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class SecretSourceIT extends AbstractPluginIT {

    private static final String SECRET = "supersecret";

    private ConfigurationContext context;

    @Before
    public void refreshConfigurationContext() {
        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        context = new ConfigurationContext(registry);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretNotFound() {
        assertThatIOException()
                .isThrownBy(() -> revealSecret("foo"));
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
        final CreateSecretOperation.Result foo = createSecret(SECRET, opts -> {});

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotRevealSoftDeletedSecrets() {
        // Given
        final CreateSecretOperation.Result foo = createSecret(SECRET, opts -> {});

        // When
        deleteSecret(foo.getName());
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEmpty();
    }

    private String revealSecret(String id) {
        return SecretSourceResolver.resolve(context, "${" + id + "}");
    }
}
