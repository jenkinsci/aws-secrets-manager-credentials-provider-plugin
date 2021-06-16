package io.jenkins.plugins.credentials.secretsmanager.factory.git_app;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension(optional = true)
public class GitCredentialFactory {
    private static final Logger LOG = Logger.getLogger(GitCredentialFactory.class.getName());

    public Optional<StandardCredentials> createCredential(String name, String description, String appId, Secret secret) {
        if (Jenkins.get().getPlugin("github-branch-source") == null) {
            LOG.warning("Plugin not installed: github-branch-source. Cannot create type: " + Type.githubApp);
            return Optional.empty();
        }

        return Optional.of(new GitHubAppCredentials(CredentialsScope.GLOBAL, name, description, appId, secret));
    }
}

