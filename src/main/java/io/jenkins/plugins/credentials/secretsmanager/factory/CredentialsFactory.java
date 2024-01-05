package io.jenkins.plugins.credentials.secretsmanager.factory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.credentials.secretsmanager.Messages;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CredentialsFactory {

    private CredentialsFactory() {

    }

    public static Optional<StandardCredentials> create(String arn, String name, String description, Map<String, String> tags, AWSSecretsManager client) {
        final var type = tags.getOrDefault("type", "");

        // Collectors.toMap ensures the factories are unique
        final var factories = ServiceLoader.load(AwsCredentialsFactory.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toMap(AwsCredentialsFactory::getType, Function.identity()));

        final var factory = Optional.ofNullable(factories.get(type));

        final var tagsProvider = new NamespacedTags(tags);

        return factory.stream()
                .map(f -> f.create(arn, name, description, tagsProvider, client))
                .flatMap(Optional::stream)
                .findFirst();
    }

    public static class RealSecretsManager {

        private static final Logger LOG = Logger.getLogger(RealSecretsManager.class.getName());

        private final String id;
        private final transient AWSSecretsManager client;

        protected RealSecretsManager(AWSSecretsManager client, String id) {
            this.client = client;
            this.id = id;
        }

        @NonNull
        protected SecretValue getSecretValue() {
            try {
                final GetSecretValueResult result = client.getSecretValue(new GetSecretValueRequest().withSecretId(id));
                if (result.getSecretBinary() != null) {
                    return SecretValue.binary(result.getSecretBinary().array());
                }
                if (result.getSecretString() != null) {
                    return SecretValue.string(result.getSecretString());
                }
                throw new IllegalStateException(Messages.emptySecretError(id));
            } catch (AmazonClientException ex) {
                LOG.warning("AWS Secrets Manager retrieval error");
                LOG.warning(ex.getMessage());

                throw new CredentialsUnavailableException("secret", Messages.couldNotRetrieveCredentialError(id));
            }
        }
    }
}
