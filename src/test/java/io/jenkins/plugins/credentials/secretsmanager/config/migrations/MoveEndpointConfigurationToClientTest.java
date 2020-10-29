package io.jenkins.plugins.credentials.secretsmanager.config.migrations;

import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class MoveEndpointConfigurationToClientTest extends MigrationTest {
    @Override
    protected void change(PluginConfiguration config) {
        assertSoftly(s -> {
            s.assertThat(config.getClient().getEndpointConfiguration())
                    .as("New property")
                    .isEqualTo(new EndpointConfiguration("http://localhost:4584", "us-east-1"));
            s.assertThat(config)
                    .extracting("endpointConfiguration")
                    .as("Old property")
                    .isNull();
        });
    }
}
