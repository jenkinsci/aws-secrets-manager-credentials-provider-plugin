package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.credentials.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import io.jenkins.plugins.credentials.secretsmanager.util.Lists;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public abstract class AbstractFiltersIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setFilters(Filter... filters);

    @Test
    public void shouldCustomiseFilters() {
        // Given
        setFilters(new Filter("name", Lists.of(new Value("foo"))));

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getListSecrets().getFilters())
                .extracting("key", "values")
                .contains(tuple("name", Lists.of(new Value("foo"))));
    }
}
