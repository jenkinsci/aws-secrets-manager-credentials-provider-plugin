package io.jenkins.plugins.credentials.secretsmanager.config.endpointConfiguration;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebEndpointConfigurationIT extends AbstractEndpointConfigurationIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setEndpointConfiguration(serviceEndpoint, signingRegion);
        });
    }

    @Test
    public void shouldCustomiseAndResetConfiguration() {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.setEndpointConfiguration("http://localhost:4584", "us-east-1");
        });

        final PluginConfiguration configBefore = getPluginConfiguration();

        assertThat(configBefore.getEndpointConfiguration()).isNotNull();

        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.clear();
        });

        final PluginConfiguration configAfter = getPluginConfiguration();

        assertThat(configAfter.getEndpointConfiguration()).isNull();
    }
}
