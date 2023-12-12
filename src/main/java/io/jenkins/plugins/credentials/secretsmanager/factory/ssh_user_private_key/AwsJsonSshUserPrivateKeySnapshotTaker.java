package io.jenkins.plugins.credentials.secretsmanager.factory.ssh_user_private_key;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsJsonSshUserPrivateKeySnapshotTaker extends CredentialsSnapshotTaker<AwsJsonSshUserPrivateKey> {
    @Override
    public Class<AwsJsonSshUserPrivateKey> type() {
        return AwsJsonSshUserPrivateKey.class;
    }

    @Override
    public AwsJsonSshUserPrivateKey snapshot(AwsJsonSshUserPrivateKey credential) {
        return new AwsJsonSshUserPrivateKey(credential);
    }
}
