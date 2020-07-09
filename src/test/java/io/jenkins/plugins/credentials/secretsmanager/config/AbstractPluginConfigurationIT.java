package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public abstract class AbstractPluginConfigurationIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setEndpointConfiguration(String serviceEndpoint, String signingRegion);

    protected abstract void setTagFilters(String key, String value);

    protected abstract void setNameFilters(String pattern);

    @Test
    public void shouldHaveDefaultConfiguration() {
        final PluginConfiguration config = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(config.getFilters()).as("Filters").isNull();
        });
    }

    @Test
    public void shouldCustomiseEndpointConfiguration() {
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

    @Test
    public void shouldCustomiseTagFilter() {
        // Given
        setTagFilters("product", "foobar");

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFilters().getTag())
                .extracting("key", "value")
                .containsOnly("product", "foobars");
    }

    @Test
    public void shouldCustomiseNameFilter() {
        // Given
        setNameFilters("dev");

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFilters().getName())
                .extracting("id", "pattern")
                .containsOnly("del");
    }
}
