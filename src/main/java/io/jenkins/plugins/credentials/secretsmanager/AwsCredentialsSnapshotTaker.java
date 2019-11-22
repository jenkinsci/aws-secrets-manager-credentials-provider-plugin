package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;

import hudson.Extension;

@Extension
@SuppressWarnings("unused")
public class AwsCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsCredentials> {
    @Override
    public Class<AwsCredentials> type() {
        return AwsCredentials.class;
    }

    @Override
    public AwsCredentials snapshot(AwsCredentials credential) {
        final SecretValue result = credential.getSecretValue();
        return new AwsCredentialsSnapshot(credential.getId(), credential.getDescription(), credential.getTags(), result);
    }
}
