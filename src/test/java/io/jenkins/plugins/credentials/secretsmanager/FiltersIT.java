package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Maps;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FiltersIT extends AbstractPluginIT {
    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldFilterByTag() {
        // Given
        final CreateSecretOperation.Result foo = createSecret("supersecret", opts -> {
            opts.tags = Maps.of(
                    "jenkins:credentials:type", "string",
                    "product", "roadrunner");
        });
        // And
        final CreateSecretOperation.Result bar = createOtherSecret("supersecret", opts -> {
            opts.tags = Maps.of(
                    "jenkins:credentials:type", "string",
                    "product", "coyote");
        });

        // When
        final List<StringCredentials> credentials = lookupCredentials(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }
}
