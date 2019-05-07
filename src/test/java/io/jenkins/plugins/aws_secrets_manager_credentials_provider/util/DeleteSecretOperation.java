package io.jenkins.plugins.aws_secrets_manager_credentials_provider.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;

import java.util.function.Consumer;

public class DeleteSecretOperation {

    private final AWSSecretsManager client;

    public DeleteSecretOperation(AWSSecretsManager client) {
        this.client = client;
    }

    public void run(String secretId) {
        run(secretId, o -> {});
    }

    public void run(String secretId, Consumer<Opts> opts) {
        final Opts o = new Opts();
        opts.accept(o);

        final DeleteSecretRequest request = new DeleteSecretRequest();

        request.setSecretId(secretId);

        if (o.force) {
            request.setForceDeleteWithoutRecovery(true);
        }

        try {
            client.deleteSecret(request);
        } catch (ResourceNotFoundException e) {
            // Don't care
        }
    }

    public static class Opts {
        public boolean force = false;
    }
}
