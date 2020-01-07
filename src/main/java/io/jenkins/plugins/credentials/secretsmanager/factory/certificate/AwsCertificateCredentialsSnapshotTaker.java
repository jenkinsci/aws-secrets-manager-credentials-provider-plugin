package io.jenkins.plugins.credentials.secretsmanager.factory.certificate;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;

import com.cloudbees.plugins.credentials.SecretBytes;
import hudson.Extension;

import java.io.Serializable;
import java.util.function.Supplier;

@Extension
@SuppressWarnings("unused")
public class AwsCertificateCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsCertificateCredentials> {
    @Override
    public Class<AwsCertificateCredentials> type() {
        return AwsCertificateCredentials.class;
    }

    @Override
    public AwsCertificateCredentials snapshot(AwsCertificateCredentials credential) {
        final SecretBytes result = credential.getSecretBytes();
        return new AwsCertificateCredentials(credential.getId(), credential.getDescription(), new SecretBytesSnapshot(result));
    }

    private static class SecretBytesSnapshot implements Supplier<SecretBytes>, Serializable {

        private final SecretBytes value;

        private SecretBytesSnapshot(SecretBytes value) {
            this.value = value;
        }

        @Override
        public SecretBytes get() {
            return value;
        }
    }
}
