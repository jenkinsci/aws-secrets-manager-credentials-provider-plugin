package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.security.ACL;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.EnvVarsRule;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SecretSourceIT {

    private static final String SECRET_STRING = "supersecret";
    private static final byte[] SECRET_BINARY = {0x01, 0x02, 0x03};

    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();
    public final JenkinsRule jenkins = new JenkinsConfiguredWithCodeRule();

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
        final String secret = revealSecret(AWSSecretsManagerRule.FOO);

        // Then
        assertThat(secret).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecret() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET_STRING, opts -> {});

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretWasSoftDeleted() {
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET_STRING, opts -> {});
        secretsManager.deleteSecret(foo.getName());

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldThrowExceptionWhenSecretWasBinary() {
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET_BINARY, opts -> {});

        assertThatIOException()
                .isThrownBy(() -> revealSecret(foo.getName()));
    }

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldIgnoreFilters() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET_STRING, opts -> {
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
        final CreateSecretOperation.Result foo = secretsManager.createSecret(SECRET_STRING, opts -> {});

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as(SecretSource.class.getName()).isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredentials(StandardCredentials.class)).as(CredentialsProvider.class.getName()).isEmpty();
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRevealSecretsInsideCredentialsProvider() {
        final CreateSecretOperation.Result foo = secretsManager.createStringSecret(SECRET_STRING);

        assertSoftly(s -> {
            s.assertThat(revealSecret(foo.getName())).as(SecretSource.class.getName()).isEqualTo(SECRET_STRING);
            s.assertThat(lookupCredential(StringCredentials.class, foo.getName()).getSecret().getPlainText()).as(CredentialsProvider.class.getName()).isEqualTo(SECRET_STRING);
        });
    }

    private <C extends StandardCredentials> C lookupCredential(Class<C> type, String id) {
        return lookupCredentials(type).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected a credential but none was present"));
    }

    <C extends Credentials> List<C> lookupCredentials(Class<C> type) {
        return CredentialsProvider.lookupCredentials(type, jenkins.jenkins, ACL.SYSTEM, Collections.emptyList());
    }

    private String revealSecret(String id) {
        return SecretSourceResolver.resolve(context, "${" + id + "}");
    }
}
