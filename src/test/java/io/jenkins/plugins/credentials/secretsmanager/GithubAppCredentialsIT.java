package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;

import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The plugin should support GitHub App credentials (from the optional github-branch-source plugin).
 * <p>
 * Pipeline binding tests are deliberately absent: reading a GitHub App credential's password makes Jenkins exchange a
 * JWT for an installation token over the network with GitHub, which is outside the scope of this plugin.
 */
public class GithubAppCredentialsIT {

    private static final String APP_ID = "11111";
    private static final String PRIVATE_KEY = Crypto.newPrivateKey();

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListView() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);
        final var credential = lookup(GitHubAppCredentials.class, secret.name());
        // GitHubAppCredentials has its own display name format (unlike this plugin's other credential types)
        final var displayName = CredentialsNameProvider.name(credential);

        // When
        final ListBoxModel list = jenkins.getCredentials().list(GitHubAppCredentials.class);

        // Then
        assertThat(list)
                .containsOption(displayName, secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveDescriptorIcon() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);
        final var theirs = new GitHubAppCredentials(CredentialsScope.GLOBAL, "name", "description", APP_ID, Secret.fromString(PRIVATE_KEY));

        // When
        final var ours = lookup(GitHubAppCredentials.class, secret.name());

        // Then
        assertThat(ours)
                .hasSameDescriptorIconAs(theirs);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportSnapshots() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);
        final var before = lookup(GitHubAppCredentials.class, secret.name());

        // When
        final var after = CredentialSnapshots.snapshot(before);

        // Then
        assertThat(after)
                .hasId(before.getId())
                .hasAppId(before.getAppID())
                .hasPrivateKey(before.getPrivateKey());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveId() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);

        // When
        final var credential = lookup(GitHubAppCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasId(secret.name());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHaveAppId() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);

        // When
        final var credential = lookup(GitHubAppCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasAppId(APP_ID);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldHavePrivateKey() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);

        // When
        final var credential = lookup(GitHubAppCredentials.class, secret.name());

        // Then
        assertThat(credential)
                .hasPrivateKey(PRIVATE_KEY);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotBreakListingWhenPrivateKeyCannotBeRetrieved() {
        // Given a GitHub App secret that exists, but has no value to retrieve
        final var unreadable = createGitHubAppSecret(APP_ID, null);
        final var readable = createGitHubAppSecret(APP_ID, PRIVATE_KEY);

        // When
        final var list = jenkins.getCredentials().list(GitHubAppCredentials.class);

        // Then the other credential is still listed
        assertThat(list)
                .containsOption(CredentialsNameProvider.name(lookup(GitHubAppCredentials.class, readable.name())), readable.name());

        // And only the unreadable credential fails, and only when its private key is used
        final var credential = lookup(GitHubAppCredentials.class, unreadable.name());
        assertThatThrownBy(credential::getPrivateKey)
                .isInstanceOf(CredentialsUnavailableException.class);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldRetrievePrivateKeyOnDemand() {
        // Given
        final var secret = createGitHubAppSecret(APP_ID, PRIVATE_KEY);
        final var credential = lookup(GitHubAppCredentials.class, secret.name());
        final var rotatedKey = Crypto.newPrivateKey();

        // When
        secretsManager.getClient().putSecretValue((b) -> {
            b.secretId(secret.name());
            b.secretString(rotatedKey);
        });

        // Then
        assertThat(credential)
                .hasPrivateKey(rotatedKey);
    }

    private CreateSecretResponse createGitHubAppSecret(String appId, String privateKey) {
        final var tags = List.of(
                AwsTags.type(Type.githubApp),
                AwsTags.appid(appId));

        return secretsManager.getClient().createSecret((b) -> {
            b.name(CredentialNames.random());
            b.secretString(privateKey);
            b.tags(tags);
        });
    }

    private <C extends StandardCredentials> C lookup(Class<C> type, String id) {
        return jenkins.getCredentials().lookup(type, id);
    }
}
