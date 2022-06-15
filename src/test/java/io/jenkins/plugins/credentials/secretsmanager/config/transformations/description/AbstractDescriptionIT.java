package io.jenkins.plugins.credentials.secretsmanager.config.transformations.description;

import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.description.Hide;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractDescriptionIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setHide();

    @Test
    public void shouldHaveDefault() {
        final PluginConfiguration config = getPluginConfiguration();

        assertThat(Optional.ofNullable(config.getTransformations()).map(Transformations::getDescription)).isEmpty();
    }

    @Test
    public void shouldSupportHide() {
        // Given
        setHide();

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getTransformations())
                .extracting("description")
                .isEqualTo(new Hide());
    }
}
