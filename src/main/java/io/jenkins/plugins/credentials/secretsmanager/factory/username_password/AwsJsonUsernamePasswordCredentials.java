package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentials;

/**
 * Similar to {@link AwsUsernamePasswordCredentials} but expects the AWS secret
 * data to be JSON containing two fields, {@value #JSON_FIELDNAME_FOR_USERNAME}
 * and {@value #JSON_FIELDNAME_FOR_PASSWORD} that provide both the username and
 * password. The secret JSON may contain other fields too, but we'll ignore
 * them.
 */
public class AwsJsonUsernamePasswordCredentials extends BaseAwsJsonCredentials
        implements StandardUsernamePasswordCredentials {
    /**
     * Name of the JSON field that we expect to be present and to contain the
     * credential's username.
     */
    @Restricted(NoExternalUse.class)
    public static final String JSON_FIELDNAME_FOR_USERNAME = "username";
    /**
     * Name of the JSON field that we expect to be present and to contain the
     * credential's password.
     */
    @Restricted(NoExternalUse.class)
    public static final String JSON_FIELDNAME_FOR_PASSWORD = "password";

    /**
     * Constructs a new instance.
     * 
     * @param id          The value for {@link #getId()}.
     * @param description The value for {@link #getDescription()}.
     * @param json        Supplies JSON containing a
     *                    {@value #JSON_FIELDNAME_FOR_USERNAME} field and a
     *                    {@value #JSON_FIELDNAME_FOR_PASSWORD} field.
     */
    public AwsJsonUsernamePasswordCredentials(String id, String description, Supplier<Secret> json) {
        super(id, description, json);
    }

    /**
     * Constructs a snapshot of an existing instance.
     * 
     * @param toBeSnapshotted The instance that contains the live data to be
     *                        snapshotted.
     */
    @Restricted(NoExternalUse.class)
    AwsJsonUsernamePasswordCredentials(AwsJsonUsernamePasswordCredentials toBeSnapshotted) {
        super(toBeSnapshotted);
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return Secret.fromString(getMandatoryField(getSecretJson(), JSON_FIELDNAME_FOR_PASSWORD));
    }

    @NonNull
    @Override
    public String getUsername() {
        return getMandatoryField(getSecretJson(), JSON_FIELDNAME_FOR_USERNAME);
    }

    @Override
    public boolean isUsernameSecret() {
        return true;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.usernamePassword();
        }

        @Override
        public String getIconClassName() {
            return "icon-credentials-userpass";
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }
}
