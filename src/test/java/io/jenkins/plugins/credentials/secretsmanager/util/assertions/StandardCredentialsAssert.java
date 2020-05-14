package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class StandardCredentialsAssert extends AbstractAssert<StandardCredentialsAssert, StandardCredentials> {

    public StandardCredentialsAssert(StandardCredentials actual) {
        super(actual, StandardCredentialsAssert.class);
    }

    public StandardCredentialsAssert(StandardCredentials actual, Class<?> klass) {
        super(actual, klass);
    }

    public StandardCredentialsAssert hasId(String id) {
        isNotNull();

        if (!Objects.equals(actual.getId(), id)) {
            failWithMessage("Expected ID to be <%s> but was <%s>", id, actual.getId());
        }

        return this;
    }

    public StandardCredentialsAssert hasSameDescriptorIconAs(StandardCredentials other) {
        isNotNull();

        final String ourIconClassName = actual.getDescriptor().getIconClassName();
        final String theirIconClassName = other.getDescriptor().getIconClassName();
        if (!Objects.equals(ourIconClassName, theirIconClassName)) {
            failWithMessage("Expected descriptor's icon class name to be <%s> but was <%s>", theirIconClassName, ourIconClassName);
        }

        return this;
    }
}
