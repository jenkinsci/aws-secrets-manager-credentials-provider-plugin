package io.jenkins.plugins.credentials.secretsmanager;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.util.CreateSecretOperation;
import io.jenkins.plugins.credentials.secretsmanager.util.Maps;
import io.jenkins.plugins.credentials.secretsmanager.util.MyJenkinsConfiguredWithCodeRule;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FiltersIT {

    @Rule
    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();

    @Rule
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Test
    @ConfiguredWithCode(value = "/tags.yml")
    public void shouldFilterByTag() {
        // Given
        final CreateSecretOperation.Result foo = secretsManager.createSecret("supersecret", opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.string,
                    "product", "roadrunner");
        });
        final CreateSecretOperation.Result bar = secretsManager.createOtherSecret("supersecret", opts -> {
            opts.tags = Maps.of(
                    Tags.type, Type.string,
                    "product", "coyote");
        });

        // When
        final List<StringCredentials> credentials = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString("supersecret")));
    }
}
