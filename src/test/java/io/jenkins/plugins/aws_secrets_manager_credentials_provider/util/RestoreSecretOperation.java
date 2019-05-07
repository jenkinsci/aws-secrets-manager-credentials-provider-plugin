package io.jenkins.plugins.aws_secrets_manager_credentials_provider.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.RestoreSecretRequest;
import com.amazonaws.services.secretsmanager.model.RestoreSecretResult;

public class RestoreSecretOperation {

    private final AWSSecretsManager client;

    public RestoreSecretOperation(AWSSecretsManager client) {
        this.client = client;
    }

    public void run(String secretId) {
        final RestoreSecretRequest request = new RestoreSecretRequest().withSecretId(secretId);

        try {
            client.restoreSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }
}
