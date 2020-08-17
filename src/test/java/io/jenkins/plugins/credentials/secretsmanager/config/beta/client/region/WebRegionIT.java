package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.region;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

public class WebRegionIT extends AbstractRegionIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setRegion(String region) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.setClientWithRegion(region);
        });
    }


}
