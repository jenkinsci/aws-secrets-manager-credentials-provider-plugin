package io.jenkins.plugins.credentials.secretsmanager.factory.string;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsStringCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsStringCredentials> {
    @Override
    public Class<AwsStringCredentials> type() {
        return AwsStringCredentials.class;
    }

    @Override
    public AwsStringCredentials snapshot(AwsStringCredentials credential) {
        return new AwsStringCredentials(credential.getId(), credential.getDescription(), new SecretSnapshot(credential.getSecret()));
    }

    private static class SecretSnapshot extends Snapshot<Secret> {
        SecretSnapshot(Secret value) {
            super(value);
        }
    }
}
