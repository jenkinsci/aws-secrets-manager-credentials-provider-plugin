package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class AwsSshUserPrivateKey extends BaseStandardCredentials implements SSHUserPrivateKey {
    private static final Secret NO_PASSPHRASE = Secret.fromString("");

    private final Supplier<String> privateKey;
    private final String username;

    public AwsSshUserPrivateKey(String id, String description, Supplier<String> privateKey, String username) {
        super(id, description);
        this.privateKey = privateKey;
        this.username = username;
    }

    @NonNull
    @Deprecated
    @Override
    public String getPrivateKey() {
        return privateKey.get();
    }

    @Override
    public Secret getPassphrase() {
        return NO_PASSPHRASE;
    }

    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(getPrivateKey());
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
            return Messages.sshUserPrivateKey();
        }

        @Override
        public String getIconClassName() {
            return "icon-ssh-credentials-ssh-key";
        }
    }
}
