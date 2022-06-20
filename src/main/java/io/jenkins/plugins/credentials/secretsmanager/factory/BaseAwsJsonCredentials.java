package io.jenkins.plugins.credentials.secretsmanager.factory;

import java.util.function.Supplier;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Base class for credentials where the AWS secret data is JSON format instead
 * of raw data.
 */
@Restricted(NoExternalUse.class)
public abstract class BaseAwsJsonCredentials extends BaseStandardCredentials {
    /** How to access the JSON */
    private final Supplier<Secret> json;

    /**
     * Constructs a new instance with new data.
     * 
     * @param id          The value for {@link #getId()}.
     * @param description The value for {@link #getDescription()}.
     * @param json        Supplies the data that {@link #getSecretJson()} will
     *                    decode.
     */
    protected BaseAwsJsonCredentials(String id, String description, Supplier<Secret> json) {
        super(id, description);
        this.json = json;
    }

    /**
     * Constructs an instance that is an unchanging snapshot of another instance.
     * 
     * @param toSnapshot The instance to be copied.
     */
    protected BaseAwsJsonCredentials(BaseAwsJsonCredentials toSnapshot) {
        super(toSnapshot.getId(), toSnapshot.getDescription());
        final Secret secretDataToSnapshot = toSnapshot.json.get();
        this.json = new Snapshot<Secret>(secretDataToSnapshot);
    }

    // Note:
    // We MUST NOT tell anyone what the JSON is, or give any hints as to its
    // contents, as that could then leak sensitive data so, if anything goes wrong,
    // we have to suppress the informative exception(s) and just tell the user that
    // it didn't work.

    /**
     * Reads the secret JSON and returns the field requested.
     * 
     * @param secretJson The {@link JSONObject} we're going to look in, which likely
     *                   came from {@link #getSecretJson()}.
     * @param fieldname  The (top-level) field that we want (which must be a
     *                   {@link String}).
     * @return The contents of that JSON field.
     * @throws CredentialsUnavailableException if the JSON is missing the field, or
     *                                         the field is not a {@link String}.
     */
    protected String getMandatoryField(@NonNull JSONObject secretJson, @NonNull String fieldname) {
        final String fieldValue;
        try {
            fieldValue = secretJson.getString(fieldname);
        } catch (JSONException | NullPointerException ex) {
            throw new CredentialsUnavailableException("secret", Messages.wrongJsonError(getId(), fieldname));
        }
        return fieldValue;
    }

    /**
     * Reads the secret JSON and returns the field requested.
     * 
     * @param secretJson The {@link JSONObject} we're going to look in, which likely
     *                   came from {@link #getSecretJson()}.
     * @param fieldname  The (top-level) field that we want (which must be a
     *                   {@link String}).
     * @return The contents of that JSON field.
     */
    protected String getOptionalField(@NonNull JSONObject secretJson, @NonNull String fieldname) {
        final String fieldValue = secretJson.optString(fieldname);
        return fieldValue;
    }

    /**
     * Reads the secret JSON and returns it.
     * 
     * @return The contents of that JSON field.
     * @throws CredentialsUnavailableException if there is no JSON, or it is not
     *                                         valid JSON.
     */
    @NonNull
    protected JSONObject getSecretJson() {
        final Secret secret = json.get();
        final String rawSecretJson = secret == null ? "" : secret.getPlainText();
        if (rawSecretJson.isEmpty()) {
            throw new CredentialsUnavailableException("secret", Messages.noValidJsonError(getId()));
        }
        final JSON parsedJson;
        try {
            parsedJson = JSONSerializer.toJSON(rawSecretJson);
        } catch (JSONException ex) {
            throw new CredentialsUnavailableException("secret", Messages.noValidJsonError(getId()));
        }
        // if we got this far then we have some syntactically-valid JSON
        // ... but it might not be a JSON object containing the field we wanted.
        final JSONObject jsonObject;
        try {
            jsonObject = (JSONObject) parsedJson;
        } catch (ClassCastException ex) {
            throw new CredentialsUnavailableException("secret", Messages.noValidJsonError(getId()));
        }
        return jsonObject;
    }
}
