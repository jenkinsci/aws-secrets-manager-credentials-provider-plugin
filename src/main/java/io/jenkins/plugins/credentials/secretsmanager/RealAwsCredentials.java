package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;

import java.util.Map;

import hudson.Extension;

class RealAwsCredentials extends AwsCredentials {

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    RealAwsCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description, tags);
        this.client = client;
    }

    @Override
    SecretValue getSecretValue() {
        try {
            final GetSecretValueResult result = client.getSecretValue(new GetSecretValueRequest().withSecretId(getId()));
            if (result.getSecretBinary() != null) {
                return SecretValue.binary(result.getSecretBinary().array());
            }
            if (result.getSecretString() != null) {
                return SecretValue.string(result.getSecretString());
            }
            return null;
        } catch (AmazonClientException ex) {
            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(getId()));
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends AwsCredentials.DescriptorImpl {

    }
}
