package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Test;

public abstract class AbstractPluginConfigurationTest {

    @Test
    public abstract void shouldHaveDefaultConfiguration();

    @Test
    public abstract void shouldCustomiseEndpointConfiguration();

    @Test
    public abstract void shouldCustomiseTagFilter();
}
