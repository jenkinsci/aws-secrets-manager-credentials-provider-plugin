package io.jenkins.plugins.credentials.secretsmanager.factory.git_app;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * A GitHub App credential whose private key is retrieved from Secrets Manager on demand.
 * <p>
 * Requires the optional github-branch-source plugin. Callers MUST check that the plugin is installed before
 * touching this class, e.g. via {@link GitHubAppCredentialsFactory}.
 */
public class AwsGitHubAppCredentials extends GitHubAppCredentials {

    private static final long serialVersionUID = 1L;

    private final Supplier<Secret> privateKey;

    public AwsGitHubAppCredentials(String id, String description, String appId, Supplier<Secret> privateKey) {
        // The superclass field is never read: GitHubAppCredentials always goes through getPrivateKey()
        super(CredentialsScope.GLOBAL, id, description, appId, Secret.fromString(""));
        this.privateKey = privateKey;
    }

    @Override
    public Secret getPrivateKey() {
        return privateKey.get();
    }

    @Extension(optional = true)
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        // NB: Do not look up the upstream GitHubAppCredentials.DescriptorImpl from here. Jenkins sorts descriptors
        // by display name during extension loading, so doing so recurses infinitely.
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.githubApp();
        }

        @Override
        public String getIconClassName() {
            // Same icon as the upstream GitHubAppCredentials descriptor
            return "symbol-logo-github plugin-ionicons-api";
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }
}
