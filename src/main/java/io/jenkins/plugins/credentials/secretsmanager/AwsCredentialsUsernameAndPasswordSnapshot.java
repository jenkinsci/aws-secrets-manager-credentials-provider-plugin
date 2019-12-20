package io.jenkins.plugins.credentials.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;

import java.util.Map;

/**
 * A self-contained version of the AwsCredentials.
 */
class AwsCredentialsUsernameAndPasswordSnapshot extends AwsCredentials implements StandardUsernamePasswordCredentials {

    private static final long serialVersionUID = 1L;

    private final SecretValue result;

    AwsCredentialsUsernameAndPasswordSnapshot(String id, String description, Map<String, String> tags, SecretValue result) {
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
