package io.jenkins.plugins.credentials.secretsmanager.factory.string;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.*;

import java.util.Map;
import java.util.Optional;

public class AwsStringCredentialsFactory implements AwsCredentialsFactory {
    @Override
    public String getType() {
        return Type.string;
    }

    @Override
    public Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var cred = new AwsStringCredentials(name, description, new SecretSupplier(client, arn));
        return Optional.of(cred);
    }
}
