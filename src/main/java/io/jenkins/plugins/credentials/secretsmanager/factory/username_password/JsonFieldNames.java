package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;

/**
 * The names of the JSON fields that hold the username and the password.
 *
 * <p>By default these are {@code username} and {@code password}, but they can be customised through
 * the {@code jenkins:credentials:options} tag. The tag value is a list of {@code name=value} pairs
 * separated by {@code :}, where {@code name} is either {@code username} or {@code password} and
 * {@code value} is the JSON field name to read it from. For example:
 *
 * <pre>{@code username=keyId:password=secretAccessKey}</pre>
 *
 * <p>Specifying only one of the two is allowed; the other one keeps its default. An empty (or
 * absent) tag value also results in the default field names.
 */
public final class JsonFieldNames {

    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_PASSWORD = "password";

    private final String username;
    private final String password;

    private JsonFieldNames(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public static JsonFieldNames parse(String id, String options) {
        String username = JsonUsernamePassword.FIELD_USERNAME;
        String password = JsonUsernamePassword.FIELD_PASSWORD;

        if (options == null || options.trim().isEmpty()) {
            return new JsonFieldNames(username, password);
        }

        for (final String part : options.split(":")) {
            final String entry = part.trim();
            if (entry.isEmpty()) {
                continue;
            }
            final int eq = entry.indexOf('=');
            if (eq < 0) {
                throw new CredentialsUnavailableException(
                        "secret",
                        "The credential " + id + " has a malformed '" + Tags.options
                                + "' entry '" + entry + "' (expected 'name=value')");
            }
            final String name = entry.substring(0, eq).trim();
            final String value = entry.substring(eq + 1).trim();
            if (value.isEmpty()) {
                throw new CredentialsUnavailableException(
                        "secret",
                        "The credential " + id + " has an empty field name for '" + name
                                + "' in the '" + Tags.options + "' tag");
            }
            switch (name) {
                case OPTION_USERNAME:
                    username = value;
                    break;
                case OPTION_PASSWORD:
                    password = value;
                    break;
                default:
                    throw new CredentialsUnavailableException(
                            "secret",
                            "The credential " + id + " has an unknown option '" + name
                                    + "' in the '" + Tags.options + "' tag (expected '"
                                    + OPTION_USERNAME + "' or '" + OPTION_PASSWORD + "')");
            }
        }

        return new JsonFieldNames(username, password);
    }
}
