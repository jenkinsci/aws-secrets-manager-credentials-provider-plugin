package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;

import java.util.Map;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;

class RealBaseAwsCredentials extends AwsCredentials {

    private static final Logger LOG = Logger.getLogger(RealBaseAwsCredentials.class.getName());

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    RealBaseAwsCredentials(String id, String description, Map<String, String> tags, AWSSecretsManager client) {
        super(id, description, tags);
        this.client = client;
    }

    @NonNull
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
            throw new IllegalStateException(Messages.emptySecretError(getId()));
        } catch (AmazonClientException ex) {
            LOG.warning("AWS Secrets Manager retrieval error");
            LOG.warning(ex.getMessage());

            throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(getId()));
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends AwsCredentials.DescriptorImpl {

    }
}
