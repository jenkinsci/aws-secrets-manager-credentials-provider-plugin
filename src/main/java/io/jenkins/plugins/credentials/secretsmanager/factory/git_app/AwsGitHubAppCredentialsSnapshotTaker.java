package io.jenkins.plugins.credentials.secretsmanager.factory.git_app;

import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.factory.Snapshot;

@Extension(optional = true)
@SuppressWarnings("unused")
public class AwsGitHubAppCredentialsSnapshotTaker extends CredentialsSnapshotTaker<AwsGitHubAppCredentials> {
    @Override
    public Class<AwsGitHubAppCredentials> type() {
        return AwsGitHubAppCredentials.class;
    }

    @Override
    public AwsGitHubAppCredentials snapshot(AwsGitHubAppCredentials credential) {
        return new AwsGitHubAppCredentials(credential.getId(), credential.getDescription(), credential.getAppID(), credential.getApiUri(), new SecretSnapshot(credential.getPrivateKey()));
    }

    private static class SecretSnapshot extends Snapshot<Secret> {
        private static final long serialVersionUID = 1L;

        SecretSnapshot(Secret value) {
            super(value);
        }
    }
}
