package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;
import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials;

import java.util.Objects;

public class GithubAppCredentialsAssert extends AbstractAssert<GithubAppCredentialsAssert, GitHubAppCredentials> {

    public GithubAppCredentialsAssert(GitHubAppCredentials actual) {
        super(actual, GithubAppCredentialsAssert.class);
    }

    public GithubAppCredentialsAssert hasUsername(String username) {
        isNotNull();

        if (!Objects.equals(actual.getUsername(), username)) {
            failWithMessage("Expected username to be <%s> but was <%s>", username, actual.getUsername());
        }

        return this;
    }

    public GithubAppCredentialsAssert hasPrivateKey(String privateKey) {
        return hasPrivateKey(Secret.fromString(privateKey));
    }

    public GithubAppCredentialsAssert hasPrivateKey(Secret privateKey) {
        isNotNull();

        if (!Objects.equals(actual.getPrivateKey(), privateKey)) {
            failWithMessage("Expected private keys to be <%s> but was <%s>", privateKey, actual.getPrivateKey());
        }

        return this;
    }

    public GithubAppCredentialsAssert hasAppId(String appId) {
        isNotNull();

        if (!Objects.equals(actual.getAppID(), appId)) {
            failWithMessage("Expected App Id to be <%s> but was <%s>", appId, actual.getAppID());
        }

        return this;
    }

    public GithubAppCredentialsAssert hasSameDescriptorIconAs(StandardUsernamePasswordCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }

    public GithubAppCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

}
