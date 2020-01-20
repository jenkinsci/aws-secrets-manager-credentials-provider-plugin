package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AwsUsernamePasswordCredentials extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

    private final Supplier<Secret> password;
    private final String username;

    public AwsUsernamePasswordCredentials(String id, String description, Supplier<Secret> password, String username) {
        super(id, description);
        this.password = password;
        this.username = username;
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

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.usernamePassword();
        }
    }
}
