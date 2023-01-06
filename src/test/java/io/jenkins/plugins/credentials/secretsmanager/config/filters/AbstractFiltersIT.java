package io.jenkins.plugins.credentials.secretsmanager.config.filters;

import io.jenkins.plugins.credentials.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public abstract class AbstractFiltersIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setFilters(Filter... filters);

    @Test
    public void shouldCustomiseFilters() {
        // Given
        setFilters(new Filter("name", List.of(new Value("foo"))));

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getListSecrets().getFilters())
                .extracting("key", "values")
                .contains(tuple("name", List.of(new Value("foo"))));
    }
}
