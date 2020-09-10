package io.jenkins.plugins.credentials.secretsmanager.config.migrations;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import io.jenkins.plugins.credentials.secretsmanager.util.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ChangeFiltersToListSecretsFiltersTest extends MigrationTest {

    @Override
    public void change(PluginConfiguration config) {
        assertThat(config.getListSecrets().getFilters())
                .extracting("key", "values")
                .contains(
                        tuple("tag-key", Lists.of(new Value("foo"))),
                        tuple("tag-value", Lists.of(new Value("bar"))));
    }
}
