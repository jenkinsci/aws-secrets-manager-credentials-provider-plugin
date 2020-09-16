package io.jenkins.plugins.credentials.secretsmanager.config.transformations.name;

import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.RemovePrefix;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractNameIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setRemovePrefix(String prefix);

    @Test
    public void shouldSupportDefault() {
        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(Optional.ofNullable(config.getTransformations()).map(Transformations::getName)).isEmpty();
    }

    @Test
    public void shouldSupportRemovePrefix() {
        // Given
        final String prefix = "foo-";
        setRemovePrefix(prefix);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getTransformations())
                .extracting("name")
                .isEqualTo(new RemovePrefix(prefix));
    }
}
