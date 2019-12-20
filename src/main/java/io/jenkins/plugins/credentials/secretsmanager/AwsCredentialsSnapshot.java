package io.jenkins.plugins.credentials.secretsmanager;

import java.util.Map;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

/**
 * A self-contained version of the AwsCredentials.
 */
class AwsCredentialsSnapshot extends AwsCredentials implements StringCredentials, StandardUsernamePasswordCredentials, SSHUserPrivateKey, StandardCertificateCredentials {

    private static final long serialVersionUID = 1L;

    private final SecretValue result;

    AwsCredentialsSnapshot(String id, String description, Map<String, String> tags, SecretValue result) {
        super(id, description, tags);
        this.result = result;
    }

    @NonNull
    @Override
    SecretValue getSecretValue() {
        return result;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends AwsCredentials.DescriptorImpl {

    }
}
