package io.jenkins.plugins.credentials.secretsmanager.config.client.clientConfiguration;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.config.ClientConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractClientConfigurationIT {
    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setClientConfiguration(String nonProxyHosts, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword);

    @Test
    public void shouldHaveDefaultClientConfiguration() {
        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getClientConfiguration())
                .isNull();
    }

    @Test
    public void shouldHaveClientConfiguration() {
        // Given
        final var nonProxyHosts = "example.com";
        final var proxyHost = "localhost";
        final var proxyPort = 5000;
        final var proxyUsername = "user";
        final var proxyPassword = "fake";
        setClientConfiguration(nonProxyHosts, proxyHost, proxyPort, proxyUsername, proxyPassword);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getClientConfiguration())
                .isEqualTo(new ClientConfiguration(nonProxyHosts, proxyHost, proxyPort, proxyUsername, Secret.fromString(proxyPassword)));
    }

}
