package io.jenkins.plugins.credentials.secretsmanager.config.client.clientConfiguration;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CasCClientConfigurationIT extends AbstractClientConfigurationIT {
    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setClientConfiguration(String nonProxyHosts, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        // no-op (configured by annotations)
    }


    @Override
    @Test
    @ConfiguredWithCode("/default.yml")
    public void shouldHaveDefaultClientConfiguration() {
        super.shouldHaveDefaultClientConfiguration();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/client/clientConfiguration.yml")
    public void shouldHaveClientConfiguration() {
        super.shouldHaveClientConfiguration();
    }
}
