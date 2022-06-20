package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension
@SuppressWarnings("unused")
public class AwsJsonUsernamePasswordCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsJsonUsernamePasswordCredentials> {
    @Override
    public Class<AwsJsonUsernamePasswordCredentials> type() {
        return AwsJsonUsernamePasswordCredentials.class;
    }

    @Override
    public AwsJsonUsernamePasswordCredentials snapshot(AwsJsonUsernamePasswordCredentials credential) {
        return new AwsJsonUsernamePasswordCredentials(credential);
    }
}
