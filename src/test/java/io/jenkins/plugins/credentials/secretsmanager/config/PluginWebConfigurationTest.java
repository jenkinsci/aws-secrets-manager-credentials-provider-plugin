package io.jenkins.plugins.credentials.secretsmanager.config;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Ignore("pipeline-model-definition breaks the Web config UI with a load order bug between credentials consumers and (remote) providers")
public class PluginWebConfigurationTest extends AbstractPluginConfigurationTest {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
            form.getInputByName("_.signingRegion").setValueAttribute(signingRegion);
        });
    }

    @Override
    protected void setTagFilters(String key, String value) {
        r.configure(form -> {
            form.getInputByName("_.filters").setChecked(true);

            form.getInputByName("_.tag").setChecked(true);
            form.getInputByName("_.key").setValueAttribute(key);
            form.getInputByName("_.value").setValueAttribute(value);
        });
    }

    @Test
    public void shouldCustomiseAndResetConfiguration() {
        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute("http://localhost:4584");
            form.getInputByName("_.signingRegion").setValueAttribute("us-east-1");

            form.getInputByName("_.filters").setChecked(true);
            form.getInputByName("_.tag").setChecked(true);
            form.getInputByName("_.key").setValueAttribute("product");
            form.getInputByName("_.value").setValueAttribute("foobar");
        });

        final PluginConfiguration configBefore = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(configBefore.getEndpointConfiguration()).as("Endpoint Configuration").isNotNull();
            s.assertThat(configBefore.getFilters().getTag()).as("Filters").isNotNull();
        });

        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(false);
            form.getInputByName("_.filters").setChecked(false);
        });

        final PluginConfiguration configAfter = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(configAfter.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(configAfter.getFilters()).as("Filters").isNull();
        });
    }
}
