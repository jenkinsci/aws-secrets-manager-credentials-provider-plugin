package io.jenkins.plugins.credentials.secretsmanager.types;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl;

import java.security.KeyStore;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

class AwsCertificateCredentials extends BaseStandardCredentials implements StandardCertificateCredentials {

    private static final long serialVersionUID = 1L;
    private static final Secret NO_PASSWORD = Secret.fromString("");

    private final transient AWSSecretsManager client;

    AwsCertificateCredentials(String id, String description, AWSSecretsManager client) {
        super(id, description);
        this.client = client;
    }

    @NonNull
    @Override
    public KeyStore getKeyStore() {
        final CertificateCredentialsImpl.KeyStoreSource ksc = toKeyStoreSource(secretValue);
        return null;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return NO_PASSWORD;
    }

    private static CertificateCredentialsImpl.KeyStoreSource toKeyStoreSource(String secret) {
        final SecretBytes theCert = SecretBytes.fromString(secret);
        return new CertificateCredentialsImpl.UploadedKeyStoreSource(theCert);
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
