package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import hudson.util.Secret;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Objects;

public class StringCredentialsAssert extends StandardCredentialsAssert<StringCredentials> {
    public StringCredentialsAssert(StringCredentials actual) {
        super(actual, StringCredentialsAssert.class);
    }

    public StringCredentialsAssert hasSecret(String secret) {
        isNotNull();

        if (!Objects.equals(actual.getSecret(), Secret.fromString(secret))) {
            failWithMessage("Expected secret to be <%s> but was <%s>", secret, actual.getSecret().getPlainText());
        }

        return this;
    }
}
