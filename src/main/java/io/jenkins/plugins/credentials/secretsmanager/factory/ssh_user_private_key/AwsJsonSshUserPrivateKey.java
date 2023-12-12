package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.factory.BaseAwsJsonCredentials;

/**
 * Similar to {@link AwsSshUserPrivateKey} but expects the AWS secret data to be
 * JSON containing two or three fields, {@value #JSON_FIELDNAME_FOR_USERNAME},
 * {@value #JSON_FIELDNAME_FOR_PRIVATE_KEY} and optionally a
 * {@value #JSON_FIELDNAME_FOR_PASSPHRASE} that provide the username, key and
 * (optional) passphrase. The secret JSON may contain other fields too, but
 * we'll ignore them.
 */
public class AwsJsonSshUserPrivateKey extends BaseAwsJsonCredentials implements SSHUserPrivateKey {
    /**
     * Name of the JSON field that we expect to be present and to contain the
     * credential's username.
     */
    @Restricted(NoExternalUse.class)
    public static final String JSON_FIELDNAME_FOR_USERNAME = "username";
    /**
     * Name of the JSON field that we expect to be present and to contain the
     * credential's private key.
     */
    @Restricted(NoExternalUse.class)
    public static final String JSON_FIELDNAME_FOR_PRIVATE_KEY = "privatekey";
    /**
     * Name of the JSON field that we look for and, if present, expect it to contain
     * the credential's password. If it isn't present then we assume no passphrase.
     */
    @Restricted(NoExternalUse.class)
    public static final String JSON_FIELDNAME_FOR_PASSPHRASE = "passphrase";

    /**
     * Constructs a new instance.
     * 
     * @param id          The value for {@link #getId()}.
     * @param description The value for {@link #getDescription()}.
     * @param json        Supplies JSON containing a
     *                    {@value #JSON_FIELDNAME_FOR_USERNAME} field, a
     *                    {@value #JSON_FIELDNAME_FOR_PRIVATE_KEY} field and
     *                    optionally a {@value #JSON_FIELDNAME_FOR_PASSPHRASE}
     *                    field.
     */
    public AwsJsonSshUserPrivateKey(String id, String description, Supplier<Secret> json) {
        super(id, description, json);
    }

    /**
     * Constructs a snapshot of an existing instance.
     * 
     * @param toBeSnapshotted The instance that contains the live data to be
     *                        snapshotted.
     */
    @Restricted(NoExternalUse.class)
    AwsJsonSshUserPrivateKey(AwsJsonSshUserPrivateKey toBeSnapshotted) {
        super(toBeSnapshotted);
    }

    @NonNull
    @Deprecated
    @Override
    public String getPrivateKey() {
        return getMandatoryField(getSecretJson(), JSON_FIELDNAME_FOR_PRIVATE_KEY);
    }

    @Override
    public Secret getPassphrase() {
        return Secret.fromString(getOptionalField(getSecretJson(), JSON_FIELDNAME_FOR_PASSPHRASE));
    }

    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(getPrivateKey());
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
            return Messages.sshUserPrivateKey();
        }

        @Override
        public String getIconClassName() {
            return "icon-ssh-credentials-ssh-key";
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }
}
