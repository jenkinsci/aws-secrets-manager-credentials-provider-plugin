package io.jenkins.plugins.credentials.secretsmanager.config.cache;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CasCCacheIT extends AbstractCacheIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setCache(boolean cache) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/cache/default.yml")
    public void shouldHaveDefault() {
        super.shouldHaveDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/cache/true.yml")
    public void shouldEnableCache() {
        super.shouldEnableCache();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/cache/false.yml")
    public void shouldDisableCache() {
        super.shouldDisableCache();
    }
}
