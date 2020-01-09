package io.jenkins.plugins.credentials.secretsmanager.factory.certificate;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import com.cloudbees.plugins.credentials.SecretBytes;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

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

    private static class SecretBytesSnapshot extends Snapshot<SecretBytes> {
        private SecretBytesSnapshot(SecretBytes value) {
            super(value);
        }
    }
}
