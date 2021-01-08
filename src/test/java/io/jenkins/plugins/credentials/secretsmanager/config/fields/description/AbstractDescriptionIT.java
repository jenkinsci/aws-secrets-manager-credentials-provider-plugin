package io.jenkins.plugins.credentials.secretsmanager.config.fields.description;

import io.jenkins.plugins.credentials.secretsmanager.config.Fields;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractDescriptionIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setDescription(boolean description);

    @Test
    public void shouldHaveDefault() {
        final PluginConfiguration config = getPluginConfiguration();

        assertThat(Optional.ofNullable(config.getFields()).map(Fields::getDescription)).isEmpty();
    }

    @Test
    public void shouldEnableDescription() {
        // Given
        setDescription(true);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFields().getDescription()).isTrue();
    }

    @Test
    public void shouldDisableDescription() {
        // Given
        setDescription(false);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getFields().getDescription()).isFalse();
    }
}
