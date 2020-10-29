package io.jenkins.plugins.credentials.secretsmanager.config.client.endpointConfiguration;

import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractEndpointConfigurationIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setEndpointConfiguration(String serviceEndpoint, String signingRegion);

    @Test
    public void shouldHaveEndpointConfiguration() {
        // Given
        final String serviceEndpoint = "http://localhost:4584";
        final String signingRegion = "us-east-1";
        setEndpointConfiguration(serviceEndpoint, signingRegion);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getEndpointConfiguration())
                .isEqualTo(new EndpointConfiguration(serviceEndpoint, signingRegion));
    }
}
