package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.ProxyConfiguration;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class ClientTest {

    @Test
    public void shouldConvertProxyConfigurationToAwsProxyConfiguration() {
        // Given
        final var host = "localhost";
        final var port = 8000;
        final var noProxyHost = "example.com";
        final var username = "foo";
        final var password = "fake";
        final var proxyConfiguration = new ProxyConfiguration(host, port, username, password, noProxyHost);

        // When
        final var awsProxyConfiguration = Client.toAwsProxyConfiguration(proxyConfiguration);

        // Then
        assertSoftly(s -> {
           s.assertThat(awsProxyConfiguration.host()).as("Host").isEqualTo(host);
           s.assertThat(awsProxyConfiguration.port()).as("Port").isEqualTo(port);
           s.assertThat(awsProxyConfiguration.username()).as("Username").isEqualTo(username);
           s.assertThat(awsProxyConfiguration.password()).as("Password").isEqualTo(password);
           s.assertThat(awsProxyConfiguration.nonProxyHosts()).as("Non-Proxy Hosts").isEqualTo(Set.of(noProxyHost));
        });
    }
}
