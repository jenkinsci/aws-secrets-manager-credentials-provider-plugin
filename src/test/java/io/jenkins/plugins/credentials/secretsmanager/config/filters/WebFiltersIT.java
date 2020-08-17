package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

public class WebFiltersIT extends AbstractFiltersIT {
    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setTagFilters(String key, String value) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setFilter(key, value);
        });
    }
}
