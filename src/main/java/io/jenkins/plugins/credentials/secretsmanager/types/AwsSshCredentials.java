package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

class AwsSshCredentials extends BaseStandardCredentials implements SSHUserPrivateKey {

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    AwsSshCredentials(String id, String description, AWSSecretsManager client) {
        super(id, description);
        this.client = client;
    }

    @NonNull
    @Override
    public String getPrivateKey() {
        return null;
    }

    /**
     *
     * @return null (there is no passphrase for AWS SSH credentials)
     */
    @Override
    public Secret getPassphrase() {
        return null;
    }

    @NonNull
    @Override
    public List<String> getPrivateKeys() {
        return Collections.singletonList(this.getPrivateKey());
    }

    @NonNull
    @Override
    public String getUsername() {
        return null;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.sshUserPrivateKey();
        }
    }
}
