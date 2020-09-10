package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.EnvVarsRule;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SecretSourceIT {

    private static final String SECRET_STRING = "supersecret";
    private static final byte[] SECRET_BINARY = {0x01, 0x02, 0x03};

    public final AWSSecretsManagerRule secretsManager = new AutoErasingAWSSecretsManagerRule();
    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(new EnvVarsRule()
                    .set("AWS_ACCESS_KEY_ID", "fake")
                    .set("AWS_SECRET_ACCESS_KEY", "fake")
                    // Invent 2 environment variables which don't technically exist in AWS SDK
                    .set("AWS_SERVICE_ENDPOINT", "http://localhost:4584")
                    .set("AWS_SIGNING_REGION", "us-east-1"))
            .around(jenkins)
            .around(secretsManager);

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
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldReturnEmptyWhenSecretWasNotFound() {
        // When
        final String secret = revealSecret("foo");

        // Then
        assertThat(secret).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecret() {
        // Given
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of());

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretWasSoftDeleted() {
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of());
        deleteSecret(foo.getName());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretWasBinary() {
        final CreateSecretResult foo = createSecret(SECRET_BINARY, Lists.of());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/filters.yml")
    public void shouldIgnoreFilters() {
        // Given
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of(AwsTags.tag("wrong", "tag")));

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecretsOutsideCredentialsProvider() {
        final CreateSecretResult foo = createSecret(SECRET_STRING, Lists.of());

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as("SecretSource").isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredentials(StandardCredentials.class)).as("CredentialsProvider").isEmpty();
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecretsInsideCredentialsProvider() {
        final CreateSecretResult foo = createStringSecret(SECRET_STRING);

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as("SecretSource").isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredential(StringCredentials.class, foo.getName()).getSecret().getPlainText()).as("CredentialsProvider").isEqualTo(SECRET_STRING);
        });
    }

    private <C extends StandardCredentials> C lookupCredential(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }

    private <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return jenkins.getCredentials().lookup(type);
    }

    private CreateSecretResult createStringSecret(String secretString) {
        final List<Tag> tags = Lists.of(AwsTags.type(Type.string));

        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
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
