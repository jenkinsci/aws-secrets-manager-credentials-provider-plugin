package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PluginWebConfigurationIT extends AbstractPluginConfigurationIT {

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

    @Override
    protected void setTagFilters(String key, String value) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setFilter(key, value);
        });
    }

    @Override
    protected void setClients(String role) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setClient(role, null);
        });
    }

    @Test
    public void shouldCustomiseAndResetConfiguration() {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.setEndpointConfiguration("http://localhost:4584", "us-east-1");
            form.setFilter("product", "foobar");
            form.setClient("arn:aws:iam::123456789012:role/marketingadminrole", null);
        });

        final PluginConfiguration configBefore = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(configBefore.getEndpointConfiguration()).as("Endpoint Configuration").isNotNull();
            s.assertThat(configBefore.getFilters().getTag()).as("Filters").isNotNull();
            s.assertThat(configBefore.getBeta().getClients().build()).as("Clients").isNotEmpty();
        });

        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            form.clear();
        });

        final PluginConfiguration configAfter = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(configAfter.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(configAfter.getFilters()).as("Filters").isNull();
            s.assertThat(configAfter.getBeta().getClients()).as("Clients").isNull();
        });
    }
}
