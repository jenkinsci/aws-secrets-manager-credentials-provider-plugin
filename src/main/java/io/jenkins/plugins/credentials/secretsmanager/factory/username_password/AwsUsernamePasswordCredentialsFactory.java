package io.jenkins.plugins.credentials.secretsmanager.factory.username_password;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.AwsCredentialsFactory;
import io.jenkins.plugins.credentials.secretsmanager.factory.SecretSupplier;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;

import java.util.Map;
import java.util.Optional;

public class AwsUsernamePasswordCredentialsFactory implements AwsCredentialsFactory {
    @Override
    public String getType() {
        return Type.usernamePassword;
    }

    @Override
    public Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var username = tags.getOrDefault(Tags.username, "");
        final var cred = new AwsUsernamePasswordCredentials(name, description, new SecretSupplier(client, arn), username);
        return Optional.of(cred);
    }
}
