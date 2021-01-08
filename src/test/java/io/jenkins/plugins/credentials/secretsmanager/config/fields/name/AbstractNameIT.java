package io.jenkins.plugins.credentials.secretsmanager.config.fields.name;

import io.jenkins.plugins.credentials.secretsmanager.config.Fields;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.RemovePrefix;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractNameIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setName(String prefix);

    @Test
    public void shouldSupportDefault() {
        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(Optional.ofNullable(config.getFields()).map(Fields::getName)).isEmpty();
    }

    @Test
    public void shouldSupportRemovePrefix() {
        // Given
        final String prefix = "foo-";
        setName(prefix);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFields())
                .extracting("name")
                .isEqualTo(new RemovePrefix(prefix));
    }
}
