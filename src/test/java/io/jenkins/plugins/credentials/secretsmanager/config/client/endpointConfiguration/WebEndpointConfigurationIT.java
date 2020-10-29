package io.jenkins.plugins.credentials.secretsmanager.config.client.endpointConfiguration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import org.junit.Rule;

public class WebEndpointConfigurationIT extends AbstractEndpointConfigurationIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
            form.getSelectByName("_.signingRegion").setSelectedAttribute(signingRegion, true);
        });
    }
}
