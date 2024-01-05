package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.ProxyConfiguration;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class ClientTest {

    @Test
    public void shouldConvertProxyConfigurationToClientConfiguration() {
        // Given
        final var host = "localhost";
        final var port = 8000;
        final var noProxyHost = "example.com";
        final var username = "foo";
        final var password = "fake";
        final var proxyConfiguration = new ProxyConfiguration(host, port, username, password, noProxyHost);

        // When
        final var clientConfiguration = Client.toClientConfiguration(proxyConfiguration);

        // Then
        assertSoftly(s -> {
           s.assertThat(clientConfiguration.getProxyHost()).as("Host").isEqualTo(host);
           s.assertThat(clientConfiguration.getProxyPort()).as("Port").isEqualTo(port);
           s.assertThat(clientConfiguration.getProxyUsername()).as("Username").isEqualTo(username);
           s.assertThat(clientConfiguration.getProxyPassword()).as("Password").isEqualTo(password);
           s.assertThat(clientConfiguration.getNonProxyHosts()).as("Non-Proxy Hosts").isEqualTo(noProxyHost);
        });
    }
}
