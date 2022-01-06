package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.Tag;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CredentialNames;
import io.jenkins.plugins.credentials.secretsmanager.util.Lists;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.jvnet.hudson.test.JenkinsRule;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class SecretSourceIT {

    private static final String SECRET_STRING = "supersecret";
    private static final byte[] SECRET_BINARY = {0x01, 0x02, 0x03};

    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();
    public final JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    private ConfigurationContext context;

    @Before
    public void refreshConfigurationContext() {
        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        context = new ConfigurationContext(registry);
    }

    /**
     * Note: When Secrets Manager is unavailable, the AWS SDK treats this the same as '404 not found'.
     */
    @Test
    public void shouldReturnEmptyWhenSecretWasNotFound() {
        // When
        final String secret = revealSecret("foo");

        // Then
        assertThat(secret).isEmpty();
    }

    @Test
    public void shouldRevealSecret() {
        // Given
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of());

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    public void shouldThrowExceptionWhenSecretWasSoftDeleted() {
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of());
        deleteSecret(foo.getName());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    public void shouldThrowExceptionWhenSecretWasBinary() {
        final CreateSecretResult foo = createSecret(SECRET_BINARY, Lists.of());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    private CreateSecretResult createSecret(String secretString, List<Tag> tags) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private CreateSecretResult createSecret(byte[] secretBinary, List<Tag> tags) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretBinary(ByteBuffer.wrap(secretBinary))
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private void deleteSecret(String secretId) {
        final DeleteSecretRequest request = new DeleteSecretRequest().withSecretId(secretId);
        secretsManager.getClient().deleteSecret(request);
    }

    private String revealSecret(String id) {
        return SecretSourceResolver.resolve(context, "${" + id + "}");
    }
}
