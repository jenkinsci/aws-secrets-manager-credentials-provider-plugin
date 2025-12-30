package io.jenkins.plugins.credentials.secretsmanager;

import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FiltersIT {

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/filters.yml")
    public void shouldFilterCredentials() {
        // Given
        final var foo = createSecretWithTag("product", "foo");
        final var bar = createSecretWithTag("product", "bar");

        // When
        final var credentials = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id")
                .contains(foo.name())
                .doesNotContain(bar.name());
    }

    private CreateSecretResponse createSecretWithTag(String key, String value) {
        return createSecret("supersecret", List.of(AwsTags.type(Type.string), AwsTags.tag(key, value)));
    }

    private CreateSecretResponse createSecret(String secretString, List<Tag> tags) {
        return secretsManager.getClient().createSecret((b) -> {
            b.name(CredentialNames.random());
            b.secretString(secretString);
            b.tags(tags);
        });
    }
}
