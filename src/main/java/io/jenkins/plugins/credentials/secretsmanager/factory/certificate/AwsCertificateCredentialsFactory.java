package io.jenkins.plugins.credentials.secretsmanager.factory.certificate;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.factory.*;

import java.util.Map;
import java.util.Optional;

public class AwsCertificateCredentialsFactory implements AwsCredentialsFactory {
    @Override
    public String getType() {
        return Type.certificate;
    }

    @Override
    public Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var cred = new AwsCertificateCredentials(name, description, new SecretBytesSupplier(client, arn));
        return Optional.of(cred);
    }
}
