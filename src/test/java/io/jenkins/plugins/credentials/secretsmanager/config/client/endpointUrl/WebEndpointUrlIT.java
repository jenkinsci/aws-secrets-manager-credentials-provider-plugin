package io.jenkins.plugins.credentials.secretsmanager.config.client.endpointUrl;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import org.junit.Rule;

public class WebEndpointUrlIT extends AbstractEndpointUrlIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setEndpointUrl(String endpointUrl) {
        r.configure(form -> {
            form.getInputByName("_.endpointUrl").setValue(endpointUrl);
        });
    }
}
