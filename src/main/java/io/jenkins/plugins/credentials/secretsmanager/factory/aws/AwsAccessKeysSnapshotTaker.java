package io.jenkins.plugins.credentials.secretsmanager.factory.aws;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsAccessKeysSnapshotTaker extends CredentialsSnapshotTaker<AwsAccessKeysCredentials> {
    @Override
    public Class<AwsAccessKeysCredentials> type() {
        return AwsAccessKeysCredentials.class;
    }

    @Override
    public AwsAccessKeysCredentials snapshot(AwsAccessKeysCredentials credential) {
        return new AwsAccessKeysCredentials(credential.getId(), credential.getDescription(), new SecretSnapshot(Secret.fromString(credential.getCredentials().getAWSSecretKey())), credential.getAwsAccessKeyId(), credential.getIamRoleArn(), credential.getIamExternalId(), credential.getIamMfaSerialNumber(), credential.getStsTokenDuration());
    }

    private static class SecretSnapshot extends Snapshot<Secret> {
        SecretSnapshot(Secret value) {
            super(value);
        }
    }
}

