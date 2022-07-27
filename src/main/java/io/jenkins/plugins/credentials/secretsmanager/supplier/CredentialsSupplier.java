package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.FiltersFactory;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.*;
import io.jenkins.plugins.credentials.secretsmanager.factory.CredentialsFactory;

import java.util.*;
import java.util.function.Function;
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

        final Function<String, String> nameFormatter = createNameFormatter(config);
        final Function<String, String> descriptionFormatter = createDescriptionFormatter(config);

        final Collection<Filter> filters = createListSecretsFilters(config);

        final AWSSecretsManager client = createClient(config);

        final ListSecretsOperation listSecretsOperation = new ListSecretsOperation(client, filters);

        final Collection<SecretListEntry> secretList = listSecretsOperation.get();

        return secretList.stream()
                .map(secretListEntry -> {
                    final String name = secretListEntry.getName();
                    final String description = Optional.ofNullable(secretListEntry.getDescription()).orElse("");

                    return secretListEntry
                            .withName(nameFormatter.apply(name))
                            .withDescription(descriptionFormatter.apply(description));
                })
                .flatMap(secretListEntry -> {
                    final String arn = secretListEntry.getARN();
                    final String name = secretListEntry.getName();
                    final String description = secretListEntry.getDescription();
                    final Map<String, String> tags = Lists.toMap(secretListEntry.getTags(), Tag::getKey, Tag::getValue);
                    final Optional<StandardCredentials> cred = CredentialsFactory.create(arn, name, description, tags, client);
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

    private static Function<String, String> createNameFormatter(PluginConfiguration config) {
        return Optional.ofNullable(config.getTransformations())
                .map(Transformations::getName)
                .orElse(new io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.Default())::transform;
    }

    private static Function<String, String> createDescriptionFormatter(PluginConfiguration config) {
        return Optional.ofNullable(config.getTransformations())
                .map(Transformations::getDescription)
                .orElse(new io.jenkins.plugins.credentials.secretsmanager.config.transformer.description.Default())::transform;
    }

    private static AWSSecretsManager createClient(PluginConfiguration config) {
        final Client clientConfig = Optional.ofNullable(config.getClient())
                .orElse(new Client(null, null, null));

        return clientConfig.build();
    }
}
