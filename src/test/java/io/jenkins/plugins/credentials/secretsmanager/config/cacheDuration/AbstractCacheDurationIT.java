package io.jenkins.plugins.credentials.secretsmanager.config.cacheDuration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCacheDurationIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setCacheDuration(int cacheDuration);

    @Test
    public void shouldCustomiseCacheDuration() {
        // Given
        setCacheDuration(60);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getCacheDuration()).isEqualTo(60);
    }
}
