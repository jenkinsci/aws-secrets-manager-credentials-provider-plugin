package io.jenkins.plugins.aws_secrets_manager_credentials_provider.config;

import org.junit.Rule;

import io.jenkins.plugins.aws_secrets_manager_credentials_provider.util.JenkinsConfiguredWithWebRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


public class PluginWebConfigurationTest extends AbstractPluginConfigurationTest {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    private PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    public void shouldHaveDefaultConfiguration() {
        final PluginConfiguration config = getPluginConfiguration();

        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration()).as("Endpoint Configuration").isNull();
            s.assertThat(config.getFilters()).as("Filters").isNull();
        });
    }

    @Override
    public void shouldCustomiseEndpointConfiguration() {
        // Given
        r.configure(form -> {
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute("http://localhost:4584");
            form.getInputByName("_.signingRegion").setValueAttribute("us-east-1");
        });

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertSoftly(s -> {
            s.assertThat(config.getEndpointConfiguration().getServiceEndpoint()).as("Service Endpoint").isEqualTo("http://localhost:4584");
            s.assertThat(config.getEndpointConfiguration().getSigningRegion()).as("Signing Region").isEqualTo("us-east-1");
        });
    }

    @Override
    public void shouldCustomiseTagFilter() {
        // Given
        r.configure(form -> {
            form.getInputByName("_.filters").setChecked(true);

            form.getInputByName("_.tag").setChecked(true);
            form.getInputByName("_.key").setValueAttribute("product");
            form.getInputByName("_.value").setValueAttribute("foobar");
        });

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFilters().getTag()).isEqualTo(new Tag("product", "foobar"));
    }
}
