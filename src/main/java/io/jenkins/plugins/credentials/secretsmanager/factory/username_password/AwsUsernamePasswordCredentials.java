package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.AwsCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AwsUsernamePasswordCredentials extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

    private final Supplier<Secret> password;
    private final String username;
    private final Boolean maskUsername;

    public AwsUsernamePasswordCredentials(String id, String description, Supplier<Secret> password, String username, Boolean maskUsername) {
        super(id, description);
        this.password = password;
        this.username = username;
        this.maskUsername = maskUsername;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return password.get();
    }

    @NonNull
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isUsernameSecret() {
        return maskUsername;
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
            return "symbol-id-card";
        }

        @Override
        public boolean isApplicable(CredentialsProvider provider) {
            return provider instanceof AwsCredentialsProvider;
        }
    }
}
