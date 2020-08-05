package io.jenkins.plugins.credentials.secretsmanager.config.beta;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientsIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Test
    @ConfiguredWithCode("/config/beta/clients/duplicates.yml")
    public void shouldDeduplicateConfigurations() {
        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getBeta().getClients().getClients())
                .hasSize(1);
    }
}
