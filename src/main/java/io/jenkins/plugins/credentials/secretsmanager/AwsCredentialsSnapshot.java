package io.jenkins.plugins.credentials.secretsmanager;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;

/**
 * A self-contained version of the AwsCredentials.
 */
class AwsCredentialsSnapshot extends AwsCredentials {

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
