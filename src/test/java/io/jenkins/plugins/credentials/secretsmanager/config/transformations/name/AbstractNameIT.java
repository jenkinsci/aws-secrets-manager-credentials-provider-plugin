package io.jenkins.plugins.credentials.secretsmanager.config.transformations.name;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.RemovePrefix;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.Prefix;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes.RemovePrefixes;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractNameIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setRemovePrefix(String prefix);

    protected abstract void setRemovePrefixes(Set<Prefix> prefixes);

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

    @Test
    public void shouldSupportRemovePrefixes() {
        // Given
        final Set<Prefix> prefixes = Collections.singleton(new Prefix("foo-"));
        setRemovePrefixes(prefixes);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getTransformations())
                .extracting("name")
                .isEqualTo(new RemovePrefixes(prefixes));
    }
}
