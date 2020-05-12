package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import org.assertj.core.api.AbstractAssert;

import java.util.List;
import java.util.Objects;

public class StandardCredentialsAssert <T extends StandardCredentials> extends AbstractAssert<StandardCredentialsAssert<T>, T> {

    public StandardCredentialsAssert(T actual) {
        super(actual, StandardCredentialsAssert.class);
    }

    public StandardCredentialsAssert(T actual, Class<?> klass) {
        super(actual, klass);
    }

    public StandardCredentialsAssert<T> hasId(String id) {
        isNotNull();

        if (!Objects.equals(actual.getId(), id)) {
            failWithMessage("Expected ID to be <%s> but was <%s>", id, actual.getId());
        }

        return this;
    }

    public StandardCredentialsAssert<T> hasSameDescriptorIconAs(StandardCredentials other) {
        isNotNull();

        final String ourIconClassName = actual.getDescriptor().getIconClassName();
        final String theirIconClassName = other.getDescriptor().getIconClassName();
        if (!Objects.equals(ourIconClassName, theirIconClassName)) {
            failWithMessage("Expected descriptor's icon class name to be <%s> but was <%s>", theirIconClassName, ourIconClassName);
        }

        return this;
    }
}
