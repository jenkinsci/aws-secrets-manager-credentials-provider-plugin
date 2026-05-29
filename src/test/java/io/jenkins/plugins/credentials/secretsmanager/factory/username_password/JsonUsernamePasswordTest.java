package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonUsernamePasswordTest {

    private static final String ID = "test-secret";

    @Test
    public void shouldParsePlainJson() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"password\":\"supersecret\"}");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldIgnoreExtraFields() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"password\":\"supersecret\",\"comment\":\"extra\"}");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldDecodeBase64Fields() {
        final String encodedUsername = base64("joe");
        final String encodedPassword = base64("supersecret");

        final var result = JsonUsernamePassword.parse(ID, "{"
                + "\"username\":\"" + encodedUsername + "\","
                + "\"username_encoding\":\"base64\","
                + "\"password\":\"" + encodedPassword + "\","
                + "\"password_encoding\":\"base64\""
                + "}");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldDecodeOnlyBase64EncodedField() {
        final String encodedPassword = base64("s:p::ass");

        final var result = JsonUsernamePassword.parse(ID, "{"
                + "\"username\":\"joe\","
                + "\"password\":\"" + encodedPassword + "\","
                + "\"password_encoding\":\"base64\""
                + "}");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("s:p::ass");
    }

    @Test
    public void shouldUseCustomFieldNames() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"keyId\":\"joe\",\"secretAccessKey\":\"supersecret\"}",
                "username=keyId:password=secretAccessKey");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldUseCustomUsernameFieldOnly() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"keyId\":\"joe\",\"password\":\"supersecret\"}",
                "username=keyId");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldUseCustomPasswordFieldOnly() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"secretAccessKey\":\"supersecret\"}",
                "password=secretAccessKey");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldUseDefaultFieldNamesWhenOptionsEmpty() {
        final var result = JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"password\":\"supersecret\"}", "");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldDecodeBase64WithCustomFieldNames() {
        final String encodedUsername = base64("joe");
        final String encodedPassword = base64("supersecret");

        final var result = JsonUsernamePassword.parse(ID, "{"
                + "\"keyId\":\"" + encodedUsername + "\","
                + "\"keyId_encoding\":\"base64\","
                + "\"secretAccessKey\":\"" + encodedPassword + "\","
                + "\"secretAccessKey_encoding\":\"base64\""
                + "}", "username=keyId:password=secretAccessKey");

        assertThat(result.username()).isEqualTo("joe");
        assertThat(result.password()).isEqualTo("supersecret");
    }

    @Test
    public void shouldRejectUnknownOption() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"password\":\"x\"}", "user=keyId"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("user");
    }

    @Test
    public void shouldRejectMalformedOption() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"password\":\"x\"}", "username"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("name=value");
    }

    @Test
    public void shouldRejectMalformedJson() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID, "not json"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("JSON");
    }

    @Test
    public void shouldRejectMissingUsername() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID, "{\"password\":\"x\"}"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("username");
    }

    @Test
    public void shouldRejectMissingPassword() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID, "{\"username\":\"x\"}"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("password");
    }

    @Test
    public void shouldRejectUnknownEncoding() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID,
                "{\"username\":\"joe\",\"username_encoding\":\"rot13\",\"password\":\"x\"}"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("rot13");
    }

    @Test
    public void shouldRejectInvalidBase64() {
        assertThatThrownBy(() -> JsonUsernamePassword.parse(ID,
                "{\"username\":\"!!!\",\"username_encoding\":\"base64\",\"password\":\"x\"}"))
                .isInstanceOf(CredentialsUnavailableException.class)
                .hasMessageContaining("base64");
    }

    private static String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
