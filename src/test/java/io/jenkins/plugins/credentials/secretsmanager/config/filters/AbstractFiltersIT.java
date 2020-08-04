package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractFiltersIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setTagFilters(String key, String value);

    @Test
    public void shouldCustomiseTagFilter() {
        // Given
        setTagFilters("product", "foobar");

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFilters().getTag())
                .extracting("key", "value")
                .containsOnly("product", "foobar");
    }
}
