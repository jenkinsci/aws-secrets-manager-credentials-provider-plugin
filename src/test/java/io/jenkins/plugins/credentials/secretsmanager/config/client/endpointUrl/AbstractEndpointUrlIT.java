package io.jenkins.plugins.credentials.secretsmanager.config.client.endpointUrl;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractEndpointUrlIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setEndpointUrl(String endpointUrl);

    @Test
    public void shouldHaveEndpointUrl() {
        // Given
        final var url = "http://localhost:4584";
        setEndpointUrl(url);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getEndpointUrl())
                .isEqualTo(url);
    }
}
