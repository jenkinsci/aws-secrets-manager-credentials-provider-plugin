package io.jenkins.plugins.credentials.secretsmanager.config.endpointConfiguration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public abstract class AbstractEndpointConfigurationIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setEndpointConfiguration(String serviceEndpoint, String signingRegion);

    @Test
    public void shouldHaveEndpointConfiguration() {
        // Given
        setEndpointConfiguration("http://localhost:4584", "us-east-1");

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration().getServiceEndpoint()).as("Service Endpoint").isEqualTo("http://localhost:4584");
            s.assertThat(config.getEndpointConfiguration().getSigningRegion()).as("Signing Region").isEqualTo("us-east-1");
        });
    }
}
