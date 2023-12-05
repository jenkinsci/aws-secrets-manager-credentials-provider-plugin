package io.jenkins.plugins.credentials.secretsmanager.config.client.clientConfiguration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import org.junit.Rule;

public class WebClientConfigurationIT extends AbstractClientConfigurationIT {
    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setClientConfiguration(String nonProxyHosts, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        r.configure(form -> {
            form.getInputByName("_.clientConfiguration").setChecked(true);
            form.getInputByName("_.nonProxyHosts").setValue(nonProxyHosts);
            form.getInputByName("_.proxyHost").setValue(proxyHost);
            form.getInputByName("_.proxyPort").setValue(String.valueOf(proxyPort));
            form.getInputByName("_.proxyUsername").setValue(proxyUsername);
            form.getInputByName("_.proxyPassword").setValue(proxyPassword);
        });
    }
}
