package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import java.security.KeyStore;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

class AwsCertificateCredentials extends BaseStandardCredentials implements StandardCertificateCredentials {

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    AwsCertificateCredentials(String id, String description, AWSSecretsManager client) {
        super(id, description);
        this.client = client;
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        return null;
    }

    @NonNull
    @Override
    public Secret getPassword() {
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
            return Messages.certificate();
        }
    }
}
