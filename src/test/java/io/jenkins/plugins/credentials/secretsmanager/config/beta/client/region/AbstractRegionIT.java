package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.region;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Region;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractRegionIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setRegion(String region);

    @Test
    public void shouldHaveRegion() {
        // Given
        final String region = "us-east-1";
        setRegion(region);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getBeta().getClients().getClients())
                .extracting("region")
                .containsOnly(new Region(region));
    }
}
