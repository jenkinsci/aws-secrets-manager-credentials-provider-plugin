package io.jenkins.plugins.credentials.secretsmanager.config.transformations.name;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.Prefix;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Set;

public class CasCNameIT extends AbstractNameIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setRemovePrefix(String prefix) {
        // no-op (configured by annotations)
    }

    @Override
    protected void setRemovePrefixes(Set<Prefix> prefixes) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/transformations/name/default.yml")
    public void shouldSupportDefault() {
        super.shouldSupportDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/transformations/name/removePrefix.yml")
    public void shouldSupportRemovePrefix() {
        super.shouldSupportRemovePrefix();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/transformations/name/removePrefixes.yml")
    public void shouldSupportRemovePrefixes() {
        super.shouldSupportRemovePrefixes();
    }
}
