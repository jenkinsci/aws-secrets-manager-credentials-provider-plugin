package io.jenkins.plugins.credentials.secretsmanager.config.cache;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCacheIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setCache(boolean cache);

    @Test
    public void shouldHaveDefault() {
        final var config = getPluginConfiguration();

        assertThat(config.getCache()).isNull();
    }

    @Test
    public void shouldEnableCache() {
        // Given
        setCache(true);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getCache()).isTrue();
    }

    @Test
    public void shouldDisableCache() {
        // Given
        setCache(false);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getCache()).isFalse();
    }
}
