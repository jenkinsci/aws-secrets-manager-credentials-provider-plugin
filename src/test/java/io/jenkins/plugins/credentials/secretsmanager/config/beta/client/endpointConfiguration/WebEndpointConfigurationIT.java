package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.endpointConfiguration;

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
            form.getInputByName("_.beta").setChecked(true);
            form.getInputByName("_.clients").setChecked(true);
            // Due to ordering, the per-client EndpointConfiguration control is first on the page
            form.getInputsByName("_.endpointConfiguration").get(0).setChecked(true);
            form.getInputsByName("_.serviceEndpoint").get(0).setValueAttribute(serviceEndpoint);
            form.getInputsByName("_.signingRegion").get(0).setValueAttribute(signingRegion);
        });
    }

}
