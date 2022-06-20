package io.jenkins.plugins.credentials.secretsmanager.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.Test;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import net.sf.json.JSONObject;

public class BaseAwsJsonCredentialsTest {
    @Test
    public void getMandatoryFieldGivenValidJsonThenReturnsValue() {
        // Given
        final String expected = "myFieldValue";
        final String fieldName = "myFieldName";
        final String json = mkJson(fieldName, expected);
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        final JSONObject jsonData = instance.getSecretJson();
        final String actual = instance.getMandatoryField(jsonData, fieldName);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getMandatoryFieldGivenValidJsonWithUnwantedDataThenReturnsValue() {
        // Given
        final String expected = "myFieldValue";
        final String fieldName = "myFieldName";
        final String json = mkJson("someOtherField", "someOtherValue", fieldName, expected);
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        final JSONObject jsonData = instance.getSecretJson();
        final String actual = instance.getMandatoryField(jsonData, fieldName);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionalFieldGivenValidJsonThenReturnsValue() {
        // Given
        final String expected = "";
        final String fieldName = "fieldWeDontHaveInTheJson";
        final String json = mkJson("oneField", "oneValue");
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        final JSONObject jsonData = instance.getSecretJson();
        final String actual = instance.getOptionalField(jsonData, fieldName);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionalFieldGivenValidJsonWithoutFieldThenReturnsEmptyString() {
        // Given
        final String expected = "";
        final String fieldName = "fieldWeDontHaveInTheJson";
        final String json = mkJson("oneField", "oneValue");
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        final JSONObject jsonData = instance.getSecretJson();
        final String actual = instance.getOptionalField(jsonData, fieldName);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getSecretJsonGivenMissingJsonThenReturnsThrows() {
        // Given
        final Secret secretJson = null;
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        CredentialsUnavailableException actual = null;
        try {
            instance.getSecretJson();
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(Messages.noValidJsonError(instance.getId()));
    }

    @Test
    public void getSecretJsonGivenEmptyJsonThenReturnsThrows() {
        // Given
        final String json = "";
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        CredentialsUnavailableException actual = null;
        try {
            instance.getSecretJson();
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(Messages.noValidJsonError(instance.getId()));
    }

    @Test
    public void getSecretJsonGivenInvalidJsonThenReturnsThrows() {
        final String json = "1234 is not valid JSON";
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        CredentialsUnavailableException actual = null;
        try {
            instance.getSecretJson();
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(instance.getId());
        assertThat(actual.getMessage()).contains(Messages.noValidJsonError(instance.getId()));
        assertThat(actual.getMessage()).doesNotContain("1234");
    }

    @Test
    public void getSecretJsonGivenUnexpectedJsonThenReturnsThrows() {
        final String json = "[ \"hello\", \"world\" ]";
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);

        // When
        CredentialsUnavailableException actual = null;
        try {
            instance.getSecretJson();
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(instance.getId());
        assertThat(actual.getMessage()).contains(Messages.noValidJsonError(instance.getId()));
        assertThat(actual.getMessage()).doesNotContain("hello");
    }

    @Test
    public void getMandatoryFieldGivenValidJsonMissingDesiredFieldThenReturnsThrows() {
        // Given
        final String unexpectedFieldName = "potentiallySecretFieldName";
        final String unexpectedValue = "potentiallySecretValue";
        final String json = mkJson(unexpectedFieldName, unexpectedValue, unexpectedFieldName + "2", unexpectedValue);
        final Secret secretJson = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(secretJson);
        final TestClass instance = new TestClass(stubSupplier);
        final String missingFieldName = "someOtherFieldName";

        // When
        final JSONObject jsonObject = instance.getSecretJson();
        CredentialsUnavailableException actual = null;
        try {
            instance.getMandatoryField(jsonObject, missingFieldName);
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(instance.getId());
        assertThat(actual.getMessage()).contains(missingFieldName);
        assertThat(actual.getMessage()).contains(Messages.wrongJsonError(instance.getId(), missingFieldName));
        assertThat(actual.getMessage()).doesNotContain(unexpectedFieldName);
        assertThat(actual.getMessage()).doesNotContain(unexpectedValue);
    }

    @Test
    public void snapshotConstructorGivenInstanceToSnapshotThenReturnsClone() {
        // Given
        final String json = mkJson("oneField", "oneValue");
        final Secret secretJson = Secret.fromString(json);
        final StubSingleShotSupplier<Secret> stubSupplier = new StubSingleShotSupplier<Secret>(secretJson);
        final TestClass original = new TestClass(stubSupplier);
        final JSONObject expectedSecretJson = original.getSecretJson();
        final String expectedId = original.getId();
        final String expectedDescription = original.getDescription();
        stubSupplier.forgetPreviousCalls();

        // When
        final TestClass actual = new TestClass(original);

        // Then
        final JSONObject actualSecretJson = actual.getSecretJson();
        assertThat((Comparable<?>) actualSecretJson).isEqualTo((Comparable<?>) expectedSecretJson);
        final String actualId = actual.getId();
        assertThat(actualId).isEqualTo(expectedId);
        final String actualDescription = actual.getDescription();
        assertThat(actualDescription).isEqualTo(expectedDescription);
    }

    public static void assertThatJsonCredentialsDescriptorIsTheSameAsTheDescriptorForNonJsonCredentials(
            CredentialsDescriptor instanceUnderTest, CredentialsDescriptor nonJsonEquivalent) {
        // Given
        final CredentialsProvider awsCredProvider = new AwsCredentialsProvider();
        final CredentialsProvider otherCredProvider = new SystemCredentialsProvider.ProviderImpl();
        final String expectedDisplayName = nonJsonEquivalent.getDisplayName();
        final String expectedIconClassName = nonJsonEquivalent.getIconClassName();
        final boolean expectedApplicableToAws = nonJsonEquivalent.isApplicable(awsCredProvider);
        final boolean expectedApplicableToOther = nonJsonEquivalent.isApplicable(otherCredProvider);
        final String[] expectedDeclaredMethods = toClassAgnosticMethodDescription(
                nonJsonEquivalent.getClass().getDeclaredMethods());

        // When
        final String actualDisplayName = instanceUnderTest.getDisplayName();
        final String actualIconClassName = instanceUnderTest.getIconClassName();
        final boolean actualApplicableToAws = instanceUnderTest.isApplicable(awsCredProvider);
        final boolean actualApplicableToOther = instanceUnderTest.isApplicable(otherCredProvider);
        final String[] actualDeclaredMethods = toClassAgnosticMethodDescription(
                instanceUnderTest.getClass().getDeclaredMethods());

        // Then
        assertThat(actualDisplayName).isEqualTo(expectedDisplayName);
        assertThat(actualIconClassName).isEqualTo(expectedIconClassName);
        assertThat(actualApplicableToAws).isEqualTo(expectedApplicableToAws);
        assertThat(actualApplicableToOther).isEqualTo(expectedApplicableToOther);
        // Check no other unexpected behavior was added to one and not the other
        assertThat(actualDeclaredMethods).containsExactly(expectedDeclaredMethods);
    }

    public static String mkJson(String fieldName1, String fieldValue1) {
        return "{ \"" + fieldName1 + "\": \"" + fieldValue1 + "\" }";
    }

    public static String mkJson(String fieldName1, String fieldValue1, String fieldName2, String fieldValue2) {
        return "{ \"" + fieldName1 + "\": \"" + fieldValue1 + "\", \"" + fieldName2 + "\": \"" + fieldValue2 + "\" }";
    }

    public static String mkJson(String fieldName1, String fieldValue1, String fieldName2, String fieldValue2,
            String fieldName3, String fieldValue3) {
        return "{ \"" + fieldName1 + "\": \"" + fieldValue1 + "\", \"" + fieldName2 + "\": \"" + fieldValue2 + "\", \""
                + fieldName3 + "\": \"" + fieldValue3 + "\" }";
    }

    private static String[] toClassAgnosticMethodDescription(Method[] m) {
        final String[] results = new String[m.length];
        for (int i = 0; i < m.length; i++) {
            final Class<?> declaringClass = m[i].getDeclaringClass();
            final String original = m[i].toGenericString();
            final String definingClass = declaringClass.getName();
            final String result = original.replace(definingClass + ".", "");
            results[i] = result;
        }
        return results;
    }

    private static class TestClass extends BaseAwsJsonCredentials {
        TestClass(Supplier<Secret> usernameAndPasswordJson) {
            super("TestId", "TestDescription", usernameAndPasswordJson);
        }

        TestClass(BaseAwsJsonCredentials toSnapshot) {
            super(toSnapshot);
        }

        // expose so we can test it

        @Override
        public String getMandatoryField(JSONObject secretJson, String fieldname) {
            return super.getMandatoryField(secretJson, fieldname);
        }

        @Override
        public String getOptionalField(JSONObject secretJson, String fieldname) {
            return super.getOptionalField(secretJson, fieldname);
        }

        @Override
        public JSONObject getSecretJson() {
            return super.getSecretJson();
        }
    }

    // if we had a mocking framework like Mockito on the classpath then we wouldn't
    // need this.
    public static class StubSupplier<T> implements Supplier<T> {
        private final T supplied;

        public StubSupplier(T supplied) {
            this.supplied = supplied;
        }

        @Override
        public T get() {
            return supplied;
        }
    }

    public static class StubSingleShotSupplier<T> implements Supplier<T> {
        private final T supplied;
        private Throwable whereWeWereCalledFromTheFirstTime = null;

        public StubSingleShotSupplier(T supplied) {
            this.supplied = supplied;
        }

        public void forgetPreviousCalls() {
            whereWeWereCalledFromTheFirstTime = null;
        }

        @Override
        public T get() {
            if (whereWeWereCalledFromTheFirstTime != null) {
                throw new IllegalStateException("This provider has already been called before",
                        whereWeWereCalledFromTheFirstTime);
            }
            whereWeWereCalledFromTheFirstTime = new Throwable("First call to the supplier was from here.");
            return supplied;
        }
    }
}
