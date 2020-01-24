package io.jenkins.plugins.credentials.secretsmanager.factory.file;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import com.cloudbees.plugins.credentials.SecretBytes;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsFileCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsFileCredentials> {
    @Override
    public Class<AwsFileCredentials> type() {
        return AwsFileCredentials.class;
    }

    @Override
    public AwsFileCredentials snapshot(AwsFileCredentials credential) {
        final SecretBytes content = credential.getContentBytes();
        return new AwsFileCredentials(credential.getId(), credential.getDescription(), credential.getFileName(), new SecretBytesSnapshot(content));
    }

    private static class SecretBytesSnapshot extends Snapshot<SecretBytes> {
        private SecretBytesSnapshot(SecretBytes value) {
            super(value);
        }
    }
}
