package io.jenkins.plugins.credentials.secretsmanager.config.fields.name;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

public class WebNameIT extends AbstractNameIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setName(String prefix) {
        r.configure(form -> {
            final PluginConfigurationForm f = new PluginConfigurationForm(form);
            f.setRemovePrefixTransformation(prefix);
        });
    }
}
