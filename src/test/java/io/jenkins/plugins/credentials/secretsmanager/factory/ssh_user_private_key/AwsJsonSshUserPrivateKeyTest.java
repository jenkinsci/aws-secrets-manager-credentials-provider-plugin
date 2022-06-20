package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import static io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.mkJson;
import static io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.assertThatJsonCredentialsDescriptorIsTheSameAsTheDescriptorForNonJsonCredentials;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.StubSingleShotSupplier;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentialsTest.StubSupplier;

public class AwsJsonSshUserPrivateKeyTest {
    @Test
    public void getPassphraseGivenValidJsonThenReturnsSecretPassphrase() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String expected = "mySecretPassphrase";
        final String json = mkUsernameKeyAndPassphraseJson("myUsername", "myKey", expected);
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

        // When
        final Secret actualSecret = instance.getPassphrase();
        final String actualPassphrase = actualSecret.getPlainText();

        // Then
        assertThat(actualPassphrase).isEqualTo(expected);
    }

    @Test
    public void getPassphraseGivenValidJsonWithNoPassphraseThenReturnsEmpty() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String expected = "";
        final String json = mkUsernameKeyAndNoPassphraseJson("myUsername", "myKey");
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

        // When
        final Secret actualSecret = instance.getPassphrase();
        final String actualPassphrase = actualSecret.getPlainText();

        // Then
        assertThat(actualPassphrase).isEqualTo(expected);
    }

    @Test
    public void getPrivateKeysGivenValidJsonThenReturnsSingleKey() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final List<String> expected = Collections.singletonList("theKeyThatISet");
        final String json = mkUsernameKeyAndPassphraseJson("myUsername", expected.get(0), "mySecretPassphrase");
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

        // When
        final List<String> actual = instance.getPrivateKeys();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getUsernameGivenValidJsonThenReturnsUsername() {
        // Given
        final String id = "testId";
        final String description = "some test description";
        final String expected = "myUsername";
        final String json = mkUsernameKeyAndPassphraseJson(expected, "myKey", "mySecretPassphrase");
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

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
        final String json = mkUsernameKeyAndPassphraseJson("myUser", "myKey", "mySecretPassphrase");
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

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
        final String json = mkJson(unexpectedFieldName, unexpectedValue, unexpectedFieldName + "2", unexpectedValue);
        final Secret stubSecret = Secret.fromString(json);
        final Supplier<Secret> stubSupplier = new StubSupplier<>(stubSecret);
        final AwsJsonSshUserPrivateKey instance = new AwsJsonSshUserPrivateKey(id, description, stubSupplier);

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
        assertThat(actual.getMessage()).contains(AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_USERNAME);
        assertThat(actual.getMessage())
                .contains(Messages.wrongJsonError(id, AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_USERNAME));
        assertThat(actual.getMessage()).doesNotContain(unexpectedFieldName);
        assertThat(actual.getMessage()).doesNotContain(unexpectedValue);
    }

    @Test
    public void snapshotConstructorGivenInstanceToSnapshotThenReturnsClone() {
        // Given
        final String expectedUsername = "myUser";
        final String expectedPassphrase = "mySecretPassphrase";
        final String expectedId = "someId";
        final String expectedDescription = "someDescription";
        final String expectedKey = "someKeyThatIMade";
        final String json = mkUsernameKeyAndPassphraseJson(expectedUsername, expectedKey, expectedPassphrase);
        final Secret secretJson = Secret.fromString(json);
        final StubSingleShotSupplier<Secret> stubSupplier = new StubSingleShotSupplier<Secret>(secretJson);
        final AwsJsonSshUserPrivateKey original = new AwsJsonSshUserPrivateKey(expectedId, expectedDescription,
                stubSupplier);

        // When
        final AwsJsonSshUserPrivateKey actual = new AwsJsonSshUserPrivateKey(original);

        // Then
        final String actualId = actual.getId();
        assertThat(actualId).isEqualTo(expectedId);
        final String actualDescription = actual.getDescription();
        assertThat(actualDescription).isEqualTo(expectedDescription);
        final String actualUsername = actual.getUsername();
        assertThat(actualUsername).isEqualTo(expectedUsername);
        final String actualPassphrase = actual.getPassphrase().getPlainText();
        assertThat(actualPassphrase).isEqualTo(expectedPassphrase);
        final String actualKey = actual.getPrivateKeys().get(0);
        assertThat(actualKey).isEqualTo(expectedKey);
    }

    @Test
    public void ourDescriptorIsTheSameAsDescriptorForNonJsonCredentials() {
        // Given
        final AwsSshUserPrivateKey.DescriptorImpl expected = new AwsSshUserPrivateKey.DescriptorImpl();
        final AwsJsonSshUserPrivateKey.DescriptorImpl instance = new AwsJsonSshUserPrivateKey.DescriptorImpl();
        assertThatJsonCredentialsDescriptorIsTheSameAsTheDescriptorForNonJsonCredentials(instance, expected);
    }

    static String mkUsernameKeyAndPassphraseJson(String username, String key, String passphrase) {
        return mkJson(AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_USERNAME, username,
                AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_PASSPHRASE, passphrase,
                AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_PRIVATE_KEY, key);
    }

    private static String mkUsernameKeyAndNoPassphraseJson(String username, String key) {
        return mkJson(AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_USERNAME, username,
                AwsJsonSshUserPrivateKey.JSON_FIELDNAME_FOR_PRIVATE_KEY, key);
    }
}
