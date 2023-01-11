package io.jenkins.plugins.credentials.secretsmanager.config.migrations;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ChangeFiltersToListSecretsFiltersTest extends MigrationTest {

    @Override
    public void change(PluginConfiguration config) {
        assertThat(config.getListSecrets().getFilters())
                .extracting("key", "values")
                .contains(
                        tuple("tag-key", List.of(new Value("foo"))),
                        tuple("tag-value", List.of(new Value("bar"))));
    }
}
