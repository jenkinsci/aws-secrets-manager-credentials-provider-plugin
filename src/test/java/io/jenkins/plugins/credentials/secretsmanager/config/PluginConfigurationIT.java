package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PluginConfigurationIT {

    @Rule
    public final JenkinsRule r = new JenkinsRule();

    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Test
    public void shouldHaveDefaultConfiguration() {
        final PluginConfiguration config = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(config.getFilters()).as("Filters").isNull();
            s.assertThat(config.getBeta()).as("Beta Features").isNull();
        });
    }
}
