package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import hudson.util.Secret;
import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Objects;

public class StringCredentialsAssert extends AbstractAssert<StringCredentialsAssert, StringCredentials> {
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

    public StringCredentialsAssert hasId(String id) {
        new StandardCredentialsAssert(actual).hasId(id);

        return this;
    }

    public StringCredentialsAssert hasSameDescriptorIconAs(StringCredentials theirs) {
        new StandardCredentialsAssert(actual).hasSameDescriptorIconAs(theirs);

        return this;
    }
}
