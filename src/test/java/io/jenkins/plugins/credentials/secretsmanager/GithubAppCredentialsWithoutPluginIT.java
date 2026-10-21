package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;

import java.io.IOException;
import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;

/**
 * GitHub App credentials need the optional github-branch-source plugin. When it is not installed, GitHub App secrets
 * should be ignored, and must not affect any other credentials.
 */
public class GithubAppCredentialsWithoutPluginIT {

    private static final String GITHUB_BRANCH_SOURCE = "github-branch-source";

    public final MyJenkinsConfiguredWithCodeRule jenkins = jenkinsWithout(GITHUB_BRANCH_SOURCE);
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldIgnoreGitHubAppSecrets() {
        // Given
        org.assertj.core.api.Assertions.assertThat(jenkins.jenkins.getPlugin(GITHUB_BRANCH_SOURCE))
                .as("test precondition: %s is not installed", GITHUB_BRANCH_SOURCE)
                .isNull();
        final var githubApp = createSecret(AwsTags.type(Type.githubApp), AwsTags.appid("11111"));

        // When
        final var list = jenkins.getCredentials().list(StandardCredentials.class);

        // Then
        org.assertj.core.api.Assertions.assertThat(list)
                .noneMatch(option -> option.value.equals(githubApp.name()));
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotAffectOtherCredentials() {
        // Given
        final var githubApp = createSecret(AwsTags.type(Type.githubApp), AwsTags.appid("11111"));
        final var string = createSecret(AwsTags.type(Type.string));

        // When
        final var list = jenkins.getCredentials().list(StringCredentials.class);

        // Then
        assertThat(list)
                .containsOption(string.name(), string.name());
        org.assertj.core.api.Assertions.assertThat(list)
                .noneMatch(option -> option.value.equals(githubApp.name()));
    }

    private CreateSecretResponse createSecret(software.amazon.awssdk.services.secretsmanager.model.Tag... tags) {
        return secretsManager.getClient().createSecret((b) -> {
            b.name(CredentialNames.random());
            b.secretString("hello");
            b.tags(List.of(tags));
        });
    }

    private static MyJenkinsConfiguredWithCodeRule jenkinsWithout(String... pluginShortNames) {
        final var rule = new MyJenkinsConfiguredWithCodeRule();
        try {
            rule.with(new PluginManagerWithout(pluginShortNames));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rule;
    }
}
