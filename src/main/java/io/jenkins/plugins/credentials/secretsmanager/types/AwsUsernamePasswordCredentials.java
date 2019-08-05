package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

class AwsUsernamePasswordCredentials extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    AwsUsernamePasswordCredentials(String id, String description, AWSSecretsManager client) {
        super(id, description);
        this.client = client;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return null;
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
            return Messages.usernamePassword();
        }
    }
}
