package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsSnapshotTaker;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

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

        if (credential instanceof StringCredentials ||
                credential instanceof SSHUserPrivateKey ||
                credential instanceof StandardCertificateCredentials) {
            return new AwsCredentialsSnapshot(credential.getId(), credential.getDescription(), credential.getTags(), result);
        } else if (credential instanceof StandardUsernamePasswordCredentials) {
            return new AwsCredentialsUsernameAndPasswordSnapshot(credential.getId(), credential.getDescription(), credential.getTags(), result);
        }

        return new AwsCredentialsSnapshot(credential.getId(), credential.getDescription(), credential.getTags(), result);
    }
}
