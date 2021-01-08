package io.jenkins.plugins.credentials.secretsmanager.config.fields.name;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CasCNameIT extends AbstractNameIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setName(String prefix) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/fields/name/default.yml")
    public void shouldSupportDefault() {
        super.shouldSupportDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/fields/name/removePrefix.yml")
    public void shouldSupportRemovePrefix() {
        super.shouldSupportRemovePrefix();
    }
}
