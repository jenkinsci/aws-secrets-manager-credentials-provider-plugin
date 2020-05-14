package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class StandardUsernamePasswordCredentialsAssert extends AbstractAssert<StandardUsernamePasswordCredentialsAssert, StandardUsernamePasswordCredentials> {

    public StandardUsernamePasswordCredentialsAssert(StandardUsernamePasswordCredentials actual) {
        super(actual, StandardUsernamePasswordCredentialsAssert.class);
    }

    public StandardUsernamePasswordCredentialsAssert hasUsername(String username) {
        isNotNull();

        if (!Objects.equals(actual.getUsername(), username)) {
            failWithMessage("Expected username to be <%s> but was <%s>", username, actual.getUsername());
        }

        return this;
    }

    public StandardUsernamePasswordCredentialsAssert hasPassword(String password) {
        return hasPassword(Secret.fromString(password));
    }

    public StandardUsernamePasswordCredentialsAssert hasPassword(Secret password) {
        isNotNull();

        if (!Objects.equals(actual.getPassword(), password)) {
            failWithMessage("Expected password to be <%s> but was <%s>", password, actual.getPassword());
        }

        return this;
    }

    public StandardUsernamePasswordCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

    public StandardUsernamePasswordCredentialsAssert hasSameDescriptorIconAs(StandardUsernamePasswordCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }
}
