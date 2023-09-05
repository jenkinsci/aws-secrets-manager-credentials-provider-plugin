package io.jenkins.plugins.credentials.secretsmanager.factory.file;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.AwsCredentialsFactory;
import io.jenkins.plugins.credentials.secretsmanager.factory.SecretBytesSupplier;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;

import java.util.Map;
import java.util.Optional;

public class AwsFileCredentialsFactory implements AwsCredentialsFactory {
    @Override
    public String getType() {
        return Type.file;
    }

    @Override
    public Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var filename = tags.getOrDefault(Tags.filename, name);
        final var cred = new AwsFileCredentials(name, description, filename, new SecretBytesSupplier(client, arn));
        return Optional.of(cred);
    }
}
