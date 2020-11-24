package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.FiltersFactory;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.factory.CredentialsFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

    private static final Logger LOG = Logger.getLogger(CredentialsSupplier.class.getName());

    private CredentialsSupplier() {

    }

    public static Supplier<Collection<StandardCredentials>> standard() {
        return new CredentialsSupplier();
    }

    @Override
    public Collection<StandardCredentials> get() {
        LOG.log(Level.FINE,"Retrieve secrets from AWS Secrets Manager");

        final PluginConfiguration config = PluginConfiguration.getInstance();

        final Collection<Filter> filters = createListSecretsFilters(config);

        final AWSSecretsManager client = createClient(config);

        final ListSecretsOperation listSecretsOperation = new ListSecretsOperation(client, filters);

        final Collection<SecretListEntry> secretList = listSecretsOperation.get();

        return secretList.stream()
                .flatMap(secretListEntry -> {
                    final String name = secretListEntry.getName();
                    final String description = Optional.ofNullable(secretListEntry.getDescription()).orElse("");
                    final Map<String, String> tags = Lists.toMap(secretListEntry.getTags(), Tag::getKey, Tag::getValue);
                    final Optional<StandardCredentials> cred = CredentialsFactory.create(name, description, tags, client);
                    return Optionals.stream(cred);
                })
                .collect(Collectors.toList());
    }

    private static Collection<Filter> createListSecretsFilters(PluginConfiguration config) {
        final List<io.jenkins.plugins.credentials.secretsmanager.config.Filter> filtersConfig = Optional.ofNullable(config.getListSecrets())
                .map(ListSecrets::getFilters)
                .orElse(Collections.emptyList());

        return FiltersFactory.create(filtersConfig);
    }

    private static AWSSecretsManager createClient(PluginConfiguration config) {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();

        final Optional<AwsClientBuilder.EndpointConfiguration> endpointConfig = Optional.ofNullable(config.getEndpointConfiguration())
                .map(EndpointConfiguration::build);
        endpointConfig.ifPresent(builder::setEndpointConfiguration);

        return builder.build();
    }
}
