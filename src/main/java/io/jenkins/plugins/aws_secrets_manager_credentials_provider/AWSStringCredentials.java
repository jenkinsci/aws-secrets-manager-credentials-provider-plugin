package io.jenkins.plugins.aws_secrets_manager_credentials_provider;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.util.Secret;

public class AWSStringCredentials extends BaseStandardCredentials implements StringCredentials {

    private static final long serialVersionUID = 1L;

    private final transient AWSSecretsManager client;

    AWSStringCredentials(String id, String description, AWSSecretsManager client) {
        super(id, description);
        this.client = client;
    }

    @Nonnull
    @Override
    public Secret getSecret() {
        final String id = this.getId();

        // FIXME timeout
        final String data = getSecretValue(id);

        return Secret.fromString(data);
    }

    private String getSecretValue(String secretName) {
        final GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);

        final GetSecretValueResult result = client.getSecretValue(request);

        // Depending on whether the secret was a string or binary, one of these fields will be populated
        if (result.getSecretString() != null) {
            return result.getSecretString();
        } else {
            return StandardCharsets.UTF_8.decode(result.getSecretBinary()).toString();
        }
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        public DescriptorImpl() {
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.secretText();
        }
    }
}
