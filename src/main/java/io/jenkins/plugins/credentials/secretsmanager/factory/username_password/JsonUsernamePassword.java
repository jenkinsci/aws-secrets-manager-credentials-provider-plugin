package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Parses a username/password credential stored as a JSON document.
 *
 * <p>Expected schema:
 * <pre>{@code
 * {
 *   "username": "...",
 *   "username_encoding": "base64",   // optional
 *   "password": "...",
 *   "password_encoding": "base64"    // optional
 * }
 * }</pre>
 */
public final class JsonUsernamePassword {

    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_USERNAME_ENCODING = "username_encoding";
    public static final String FIELD_PASSWORD_ENCODING = "password_encoding";
    public static final String ENCODING_BASE64 = "base64";

    private final String username;
    private final String password;

    private JsonUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public static JsonUsernamePassword parse(String id, String secretString) {
        final JSONObject json;
        try {
            json = new JSONObject(secretString);
        } catch (JSONException ex) {
            throw new CredentialsUnavailableException(
                    "secret",
                    "Could not parse the credential " + id + " as JSON: " + ex.getMessage());
        }

        final String username = readField(id, json, FIELD_USERNAME, FIELD_USERNAME_ENCODING);
        final String password = readField(id, json, FIELD_PASSWORD, FIELD_PASSWORD_ENCODING);
        return new JsonUsernamePassword(username, password);
    }

    private static String readField(String id, JSONObject json, String field, String encodingField) {
        if (!json.has(field)) {
            throw new CredentialsUnavailableException(
                    "secret",
                    "The credential " + id + " is missing the required JSON field '" + field + "'");
        }
        final String raw = json.optString(field, null);
        if (raw == null) {
            throw new CredentialsUnavailableException(
                    "secret",
                    "The JSON field '" + field + "' of credential " + id + " must be a string");
        }
        final String encoding = json.optString(encodingField, "");
        if (encoding.isEmpty()) {
            return raw;
        }
        if (ENCODING_BASE64.equalsIgnoreCase(encoding)) {
            try {
                return new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ex) {
                throw new CredentialsUnavailableException(
                        "secret",
                        "The JSON field '" + field + "' of credential " + id
                                + " is not valid base64: " + ex.getMessage());
            }
        }
        throw new CredentialsUnavailableException(
                "secret",
                "Unsupported encoding '" + encoding + "' for JSON field '" + field
                        + "' of credential " + id);
    }
}
