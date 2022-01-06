package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.SecretSourceResolver;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CredentialNames;
import io.jenkins.plugins.credentials.secretsmanager.util.Rules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class SecretSourceIT {

    private static final String SECRET_STRING = "supersecret";

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

    @Test
    public void shouldRevealSecret() {
        // Given
        final CreateSecretResult foo = createSecret(SECRET_STRING);

        // When
        final String secret = revealSecret(foo.getName());

        // Then
        assertThat(secret).isEqualTo(SECRET_STRING);
    }

    private CreateSecretResult createSecret(String secretString) {
        final CreateSecretRequest request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString);

        return secretsManager.getClient().createSecret(request);
    }

    private String revealSecret(String id) {
        return SecretSourceResolver.resolve(context, "${" + id + "}");
    }
}
