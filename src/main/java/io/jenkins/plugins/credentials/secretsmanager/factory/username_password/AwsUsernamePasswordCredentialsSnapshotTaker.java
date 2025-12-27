package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsUsernamePasswordCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsUsernamePasswordCredentials> {
    @Override
    public Class<AwsUsernamePasswordCredentials> type() {
        return AwsUsernamePasswordCredentials.class;
    }

    @Override
    public AwsUsernamePasswordCredentials snapshot(AwsUsernamePasswordCredentials credential) {
        return new AwsUsernamePasswordCredentials(credential.getId(), credential.getDescription(), new SecretSnapshot(credential.getPassword()), credential.getUsername(), credential.isUsernameSecret());
    }

    private static class SecretSnapshot extends Snapshot<Secret> {
        SecretSnapshot(Secret value) {
            super(value);
        }
    }
}

