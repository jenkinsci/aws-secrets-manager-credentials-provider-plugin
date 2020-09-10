package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.region;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
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
        r.configure(form -> {
            form.getInputByName("_.beta").setChecked(true);
            form.getInputByName("_.clients").setChecked(true);
            // the checkbox and the text field happen to have the same name
            form.getInputsByName("_.region").get(0).setChecked(true);
            form.getInputsByName("_.region").get(1).setValueAttribute(region);
        });
    }


}
