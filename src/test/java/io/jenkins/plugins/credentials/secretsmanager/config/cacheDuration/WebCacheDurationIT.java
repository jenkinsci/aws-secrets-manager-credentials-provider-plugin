package io.jenkins.plugins.credentials.secretsmanager.config.cacheDuration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import org.junit.Rule;

public class WebCacheDurationIT extends AbstractCacheDurationIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setCacheDuration(int cacheDuration) {
        r.configure(form -> {
            form.getInputByName("_.cacheDuration").setValueAttribute(String.valueOf(cacheDuration));
        });
    }
}
