package io.jenkins.plugins.credentials.secretsmanager.factory.git_app;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import jenkins.model.Jenkins;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Creates GitHub App credentials. The github-branch-source plugin is an optional dependency, so this is the only
 * place that may reference {@link AwsGitHubAppCredentials}, and only after checking that the plugin is installed.
 */
public abstract class GitHubAppCredentialsFactory {
    private static final Logger LOG = Logger.getLogger(GitHubAppCredentialsFactory.class.getName());

    private GitHubAppCredentialsFactory() {

    }

    /**
     * @param apiUri the GitHub API endpoint, for GitHub Enterprise Server (null to use github.com)
     */
    public static Optional<StandardCredentials> create(String name, String description, String appId, @CheckForNull String apiUri, Supplier<Secret> privateKey) {
        if (Jenkins.get().getPlugin("github-branch-source") == null) {
            LOG.warning("Plugin not installed: github-branch-source. Cannot create type: " + Type.githubApp);
            return Optional.empty();
        }

        return Optional.of(new AwsGitHubAppCredentials(name, description, appId, apiUri, privateKey));
    }
}
