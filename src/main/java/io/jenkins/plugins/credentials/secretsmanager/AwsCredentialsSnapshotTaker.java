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
    public AwsCredentials snapshot(AwsCredentials c) {
        final SecretValue result = c.getSecretValue();
        return new AwsCredentialsSnapshot(c.getId(), c.getDescription(), c.getTags(), result);
    }
}
