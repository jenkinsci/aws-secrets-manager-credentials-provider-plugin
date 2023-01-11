package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CasCFiltersIT extends AbstractFiltersIT {

    @Rule
    public JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setFilters(Filter... filters) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/filters/name.yml")
    public void shouldCustomiseFilters() {
        super.shouldCustomiseFilters();
    }

    @Test
    @ConfiguredWithCode("/config/filters/multiple.yml")
    public void shouldCustomiseMultipleFilters() {
        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getListSecrets().getFilters())
                .extracting("key", "values")
                .contains(
                        tuple("tag-key", List.of(new Value("foo"))),
                        tuple("tag-value", List.of(new Value("bar"))));
    }
}
