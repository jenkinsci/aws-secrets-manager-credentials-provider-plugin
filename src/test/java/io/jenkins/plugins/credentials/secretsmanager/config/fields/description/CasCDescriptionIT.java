package io.jenkins.plugins.credentials.secretsmanager.config.fields.description;

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
    protected void setDescription(boolean description) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/fields/description/default.yml")
    public void shouldHaveDefault() {
        super.shouldHaveDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/fields/description/true.yml")
    public void shouldEnableDescription() {
        super.shouldEnableDescription();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/fields/description/false.yml")
    public void shouldDisableDescription() {
        super.shouldDisableDescription();
    }
}
