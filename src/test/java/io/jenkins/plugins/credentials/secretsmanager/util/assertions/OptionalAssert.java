package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.Optional;

/**
 * Extra assertions for Optional.
 *
 * @param <T> type of the Optional
 */
public class OptionalAssert<T> extends AbstractAssert<OptionalAssert<T>, Optional<T>> {

    public OptionalAssert(Optional<T> actual) {
        super(actual, OptionalAssert.class);
    }

    public OptionalAssert<T> isEmptyOrContains(T expectedValue) {
        // The optional wrapper itself must not be null
        isNotNull();

        final T actualValue = actual.orElse(expectedValue);

        if (!Objects.equals(actualValue, expectedValue)) {
            failWithMessage("Expected Optional to be empty or to contain <%s> but it contains <%s>", expectedValue, actualValue);
        }

        return this;
    }
}
