package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginConfigurationTest {

    private static final Duration DEFAULT_CACHE_DURATION = Duration.ofSeconds(300);
    private static final Duration MINIMUM_CACHE_DURATION = Duration.ofNanos(1);

    @Test
    public void shouldNormalizeNullToDefaultDuration() {
        assertThat(PluginConfiguration.normalize(null))
                .isEqualTo(DEFAULT_CACHE_DURATION);
    }

    @Test
    public void shouldNormalizeTrueToDefaultDuration() {
        assertThat(PluginConfiguration.normalize(true))
                .isEqualTo(DEFAULT_CACHE_DURATION);
    }

    @Test
    public void shouldNormalizeFalseToMinimumDuration() {
        assertThat(PluginConfiguration.normalize(false))
                .isEqualTo(MINIMUM_CACHE_DURATION);
    }
}
