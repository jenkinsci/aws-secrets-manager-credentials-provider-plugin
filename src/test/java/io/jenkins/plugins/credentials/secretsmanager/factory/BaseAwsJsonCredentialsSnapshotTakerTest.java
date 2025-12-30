package io.jenkins.plugins.credentials.secretsmanager.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;

import hudson.Extension;

public abstract class BaseAwsJsonCredentialsSnapshotTakerTest<C extends BaseAwsJsonCredentials, ST extends CredentialsSnapshotTaker<C>> {
    protected final Class<ST> classUnderTest;
    protected final Class<C> credentialBeingSnapshotted;

    protected BaseAwsJsonCredentialsSnapshotTakerTest(Class<ST> classUnderTest, Class<C> credentialBeingSnapshotted) {
        this.classUnderTest = classUnderTest;
        this.credentialBeingSnapshotted = credentialBeingSnapshotted;
    }

    protected abstract C makeCredential();

    private LimitedUseSupplier<?> limitedUseSupplier;

    @Before
    public void beforeTest() {
        limitedUseSupplier = null;
    }

    protected <T> Supplier<T> mkSupplier(T supplied) {
        final LimitedUseSupplier<T> result = new LimitedUseSupplier<>(supplied);
        limitedUseSupplier = result;
        return result;
    }

    private ST makeInstanceOrThrow() throws NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Constructor<ST> noArgsConstructor = classUnderTest.getConstructor();
        return noArgsConstructor.newInstance();
    }

    @Test
    public void hasNoArgsConstructor() {
        // Given

        // When
        Exception actual = null;
        try {
            makeInstanceOrThrow();
        } catch (Exception ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNull();
    }

    @Test
    public void hasExtensionAnnotation() {
        // Given

        // When
        final Extension actual = classUnderTest.getAnnotation(Extension.class);

        // Then
        assertThat(actual).isNotNull();
    }

    @Test
    public void typeTest() throws Exception {
        // Given
        final Class<C> expected = credentialBeingSnapshotted;
        final ST instance = makeInstanceOrThrow();

        // When
        final Class<C> actual = instance.type();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void snapshotGivenNullThenThrows() throws Exception {
        // Given
        final ST instance = makeInstanceOrThrow();

        // When
        Exception actual = null;
        try {
            instance.snapshot(null);
        } catch (Exception ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
    }

    @Test
    public void snapshotGivenInstanceThenCreatesSnapshot() throws Exception {
        // Given
        final ST instance = makeInstanceOrThrow();
        final C credential = makeCredential();
        final String expectedId = credential.getId();
        final String expectedDescription = credential.getDescription();
        // Use reflection to call all the getter methods
        final Map<Method, Object> expectedSnapshotContents = new IdentityHashMap<>();
        for (final Method m : credentialBeingSnapshotted.getDeclaredMethods()) {
            if (m.getParameterCount() != 0 || !m.isAccessible()) {
                continue;
            }
            final Object expectedValue;
            try {
                expectedValue = m.invoke(credential);
            } catch (IllegalAccessException e) {
                continue;
            }
            expectedSnapshotContents.put(m, expectedValue);
        }

        // When
        final C actual = instance.snapshot(credential);
        limitedUseSupplier.stopAnyFurtherCallsToTheSupplier();

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual).isNotSameAs(credential);
        assertThat(actual).isInstanceOf(credentialBeingSnapshotted);
        // ...now check it contains the right data AND that getting that data doesn't
        // trigger the supplier.
        final String actualId = actual.getId();
        assertThat(actualId).as("getId()").isEqualTo(expectedId);
        final String actualDescription = actual.getDescription();
        assertThat(actualDescription).as("getDescription").isEqualTo(expectedDescription);
        for (final Method m : expectedSnapshotContents.keySet()) {
            final Object expectedValue = expectedSnapshotContents.get(m);
            final Object actualValue = m.invoke(actual);
            assertThat(actualValue).as(m.toGenericString()).isEqualTo(expectedValue);
        }
    }

    /**
     * {@link Supplier} that can be told to stop supplying so that we can verify
     * that accessing a "snapshot" is using snapshotted values rather than still
     * using live values from here.
     *
     * @param <T> Type being supplied.
     */
    private static class LimitedUseSupplier<T> implements Supplier<T> {
        private final T supplied;
        private boolean noFurtherUsePermitted = false;

        public LimitedUseSupplier(T supplied) {
            this.supplied = supplied;
        }

        /**
         * Causes all subsequent calls to {@link #get()} to throw an
         * {@link IllegalStateException}.
         */
        public void stopAnyFurtherCallsToTheSupplier() {
            noFurtherUsePermitted = true;
        }

        @Override
        public T get() {
            if (noFurtherUsePermitted) {
                throw new IllegalStateException(
                        "Illegal access to non-snapshotted data. This data should have been copied and only the copy accessed.");
            }
            return supplied;
        }
    }
}
