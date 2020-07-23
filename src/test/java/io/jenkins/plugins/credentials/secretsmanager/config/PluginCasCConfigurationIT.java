package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PluginCasCConfigurationIT extends AbstractPluginConfigurationIT {
    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        // no-op (configured by annotations)
    }

    @Override
    protected void setTagFilters(String key, String value) {
        // no-op (configured by annotations)
    }

    @Override
    protected void setRoles(String role) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/default.yml")
    public void shouldHaveDefaultConfiguration() {
        super.shouldHaveDefaultConfiguration();
    }

    @Override
    @Test
    @ConfiguredWithCode("/custom-endpoint-configuration.yml")
    public void shouldCustomiseEndpointConfiguration() {
        super.shouldCustomiseEndpointConfiguration();
    }

    @Override
    @Test
    @ConfiguredWithCode("/custom-tag.yml")
    public void shouldCustomiseTagFilter() {
        super.shouldCustomiseTagFilter();
    }

    @Override
    @Test
    @ConfiguredWithCode("/custom-roles.yml")
    public void shouldCustomiseRoles() {
        super.shouldCustomiseRoles();
    }
}
