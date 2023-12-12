package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import static io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.mkJson;
import static io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.assertThatJsonCredentialsDescriptorIsTheSameAsTheDescriptorForNonJsonCredentials;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

import org.junit.Test;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.StubSingleShotSupplier;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.StubSupplier;

public class AwsJsonUsernamePasswordCredentialsTest {
    @Test
    public void getPasswordGivenValidJsonThenReturnsSecretPassword() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String expected = "mySecretPassword";
        final String usernamePasswordJson = mkUsernameAndPasswordJson("myUsername", expected);
        final Secret stubSecret = Secret.fromString(usernamePasswordJson);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonUsernamePasswordCredentials instance = new AwsJsonUsernamePasswordCredentials(id, description,
                stubSupplier);

        // When
        final Secret actualSecret = instance.getPassword();
        final String actualPassword = actualSecret.getPlainText();

        // Then
        assertThat(actualPassword).isEqualTo(expected);
    }

    @Test
    public void getUsernameGivenValidJsonThenReturnsUsername() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String expected = "myUsername";
        final String usernamePasswordJson = mkUsernameAndPasswordJson(expected, "mySecretPassword");
        final Secret stubSecret = Secret.fromString(usernamePasswordJson);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonUsernamePasswordCredentials instance = new AwsJsonUsernamePasswordCredentials(id, description,
                stubSupplier);

        // When
        final String actual = instance.getUsername();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void isUsernameSecretGivenAnythingThenReturnsTrue() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final boolean expected = true;
        final String usernamePasswordJson = mkUsernameAndPasswordJson("myUser", "mySecretPassword");
        final Secret stubSecret = Secret.fromString(usernamePasswordJson);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonUsernamePasswordCredentials instance = new AwsJsonUsernamePasswordCredentials(id, description,
                stubSupplier);

        // When
        final boolean actual = instance.isUsernameSecret();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getUsernameGivenInvalidJsonThenReturnsThrows() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String unexpectedFieldName = "potentiallySecretFieldName";
        final String unexpectedValue = "potentiallySecretValue";
        final String usernamePasswordJson = mkJson(unexpectedFieldName, unexpectedValue, unexpectedFieldName + "2",
                unexpectedValue);
        final Secret stubSecret = Secret.fromString(usernamePasswordJson);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonUsernamePasswordCredentials instance = new AwsJsonUsernamePasswordCredentials(id, description,
                stubSupplier);

        // When
        CredentialsUnavailableException actual = null;
        try {
            instance.getUsername();
        } catch (CredentialsUnavailableException ex) {
            actual = ex;
        }

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty()).isEqualTo("secret");
        assertThat(actual.getMessage()).contains(id);
        assertThat(actual.getMessage()).contains(AwsJsonUsernamePasswordCredentials.JSON_FIELDNAME_FOR_USERNAME);
        assertThat(actual.getMessage())
                .contains(Messages.wrongJsonError(id, AwsJsonUsernamePasswordCredentials.JSON_FIELDNAME_FOR_USERNAME));
        assertThat(actual.getMessage()).doesNotContain(unexpectedFieldName);
        assertThat(actual.getMessage()).doesNotContain(unexpectedValue);
    }

    @Test
    public void snapshotConstructorGivenInstanceToSnapshotThenReturnsClone() {
        // Given
        final String expectedUsername = "myUser";
        final String expectedPassword = "mySecretPassword";
        final String expectedId = "someId";
        final String expectedDescription = "someDescription";
        final String json = mkUsernameAndPasswordJson(expectedUsername, expectedPassword);
        final Secret secretJson = Secret.fromString(json);
        final StubSingleShotSupplier<Secret> stubSupplier = new StubSingleShotSupplier<Secret>(secretJson);
        final AwsJsonUsernamePasswordCredentials original = new AwsJsonUsernamePasswordCredentials(expectedId,
                expectedDescription, stubSupplier);

        // When
        final AwsJsonUsernamePasswordCredentials actual = new AwsJsonUsernamePasswordCredentials(original);

        // Then
        final String actualId = actual.getId();
        assertThat(actualId).isEqualTo(expectedId);
        final String actualDescription = actual.getDescription();
        assertThat(actualDescription).isEqualTo(expectedDescription);
        final String actualUsername = actual.getUsername();
        assertThat(actualUsername).isEqualTo(expectedUsername);
        final String actualPassword = actual.getPassword().getPlainText();
        assertThat(actualPassword).isEqualTo(expectedPassword);
    }

    @Test
    public void ourDescriptorIsTheSameAsDescriptorForNonJsonCredentials() {
        // Given
        final AwsUsernamePasswordCredentials.DescriptorImpl expected = new AwsUsernamePasswordCredentials.DescriptorImpl();
        final AwsJsonUsernamePasswordCredentials.DescriptorImpl instance = new AwsJsonUsernamePasswordCredentials.DescriptorImpl();
        assertThatJsonCredentialsDescriptorIsTheSameAsTheDescriptorForNonJsonCredentials(instance, expected);
    }

    static String mkUsernameAndPasswordJson(String username, String password) {
        return mkJson(AwsJsonUsernamePasswordCredentials.JSON_FIELDNAME_FOR_USERNAME, username,
                AwsJsonUsernamePasswordCredentials.JSON_FIELDNAME_FOR_PASSWORD, password);
    }
}
