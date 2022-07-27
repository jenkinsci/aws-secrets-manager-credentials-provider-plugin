package io.jenkins.plugins.credentials.secretsmanager.config.transformations.description;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CasCDescriptionIT extends AbstractDescriptionIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setHide() {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/transformations/description/default.yml")
    public void shouldHaveDefault() {
        super.shouldHaveDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/transformations/description/hide.yml")
    public void shouldSupportHide() {
        super.shouldSupportHide();
    }
}
