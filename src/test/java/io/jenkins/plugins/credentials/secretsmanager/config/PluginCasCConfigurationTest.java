package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PluginCasCConfigurationTest extends AbstractPluginConfigurationTest {
    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    private PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    @Test
    @ConfiguredWithCode("/default.yml")
    public void shouldHaveDefaultConfiguration() {
        final PluginConfiguration config = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(config.getFilters()).as("Filters").isNull();
        });
    }

    @Override
    @Test
    @ConfiguredWithCode("/custom-endpoint-configuration.yml")
    public void shouldCustomiseEndpointConfiguration() {
        final PluginConfiguration config = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration().getServiceEndpoint()).as("Service Endpoint").isEqualTo("http://localhost:4584");
            s.assertThat(config.getEndpointConfiguration().getSigningRegion()).as("Signing Region").isEqualTo("us-east-1");
        });
    }

    @Override
    @Test
    @ConfiguredWithCode("/custom-tag.yml")
    public void shouldCustomiseTagFilter() {
        final PluginConfiguration config = getPluginConfiguration();

        assertThat(config.getFilters().getTag())
                .extracting("key", "value")
                .containsOnly("product", "foobar");
    }
}
