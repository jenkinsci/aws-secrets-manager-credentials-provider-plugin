package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import io.jenkins.plugins.credentials.secretsmanager.util.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CacheIT {

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode("/integration.yml")
    public void shouldCacheCredentialsByDefault() {
        // Given
        final var foo = createSecretWithTag("product", "foo");
        final var bar = createSecretWithTag("product", "bar");

        // When
        final List<StringCredentials> first = jenkins.getCredentials().lookup(StringCredentials.class);
        // and
        setFilter("tag-value", "foo");
        // and
        final List<StringCredentials> second = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertSoftly(s -> {
            s.assertThat(first).as("First call").extracting("id").containsOnly(foo.getName(), bar.getName());
            s.assertThat(second).as("Second call").extracting("id").containsOnly(foo.getName(), bar.getName());
        });
    }

    @Test
    @ConfiguredWithCode("/cache.yml")
    public void shouldCacheCredentialsWhenEnabled() {
        // Given
        final var foo = createSecretWithTag("product", "foo");
        final var bar = createSecretWithTag("product", "bar");

        // When
        final List<StringCredentials> first = jenkins.getCredentials().lookup(StringCredentials.class);
        // and
        setFilter("tag-value", "foo");
        // and
        final List<StringCredentials> second = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertSoftly(s -> {
            s.assertThat(first).as("First call").extracting("id").containsOnly(foo.getName(), bar.getName());
            s.assertThat(second).as("Second call").extracting("id").containsOnly(foo.getName(), bar.getName());
        });
    }

    @Test
    @ConfiguredWithCode("/no-cache.yml")
    public void shouldNotCacheCredentialsWhenDisabled() {
        // Given
        final var foo = createSecretWithTag("product", "foo");
        final var bar = createSecretWithTag("product", "bar");

        // When
        final List<StringCredentials> first = jenkins.getCredentials().lookup(StringCredentials.class);
        // and
        setFilter("tag-value", "foo");
        // and
        final List<StringCredentials> second = jenkins.getCredentials().lookup(StringCredentials.class);

        // Then
        assertSoftly(s -> {
            s.assertThat(first).as("First call").extracting("id").containsOnly(foo.getName(), bar.getName());
            s.assertThat(second).as("Second call").extracting("id").containsOnly(foo.getName());
        });
    }

    private void setFilter(String key, String value) {
        final var filters = List.of(new Filter(key, List.of(new Value(value))));
        final var listSecrets = new ListSecrets(filters);
        final var config = (PluginConfiguration) jenkins.getInstance().getDescriptor(PluginConfiguration.class);
        config.setListSecrets(listSecrets);
    }

    private CreateSecretResult createSecretWithTag(String key, String value) {
        return createSecret("supersecret", List.of(AwsTags.type(Type.string), AwsTags.tag(key, value)));
    }

    private CreateSecretResult createSecret(String secretString, List<Tag> tags) {
        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

}
