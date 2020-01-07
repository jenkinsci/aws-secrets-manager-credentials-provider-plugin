package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsSshUserPrivateKeySnapshotTaker extends CredentialsSnapshotTaker<AwsSshUserPrivateKey> {
    @Override
    public Class<AwsSshUserPrivateKey> type() {
        return AwsSshUserPrivateKey.class;
    }

    @Override
    public AwsSshUserPrivateKey snapshot(AwsSshUserPrivateKey credential) {
        return new AwsSshUserPrivateKey(credential.getId(), credential.getDescription(), new StringSnapshot(credential.getPrivateKey()), credential.getUsername());
    }

    private static class StringSnapshot extends Snapshot<String> {
        StringSnapshot(String value) {
            super(value);
        }
    }
}

