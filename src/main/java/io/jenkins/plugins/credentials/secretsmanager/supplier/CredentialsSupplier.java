package io.jenkins.plugins.credentials.secretsmanager.supplier;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.Filter;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;
import software.amazon.awssdk.services.secretsmanager.model.Tag;
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

        final var config = PluginConfiguration.getInstance();

        final Function<String, String> nameFormatter = createNameFormatter(config);
        final Function<String, String> descriptionFormatter = createDescriptionFormatter(config);

        final var filters = createListSecretsFilters(config);

        final var client = createClient(config);

        final ListSecretsOperation listSecretsOperation = new ListSecretsOperation(client, filters);

        final Collection<SecretListEntry> secretList = listSecretsOperation.get();

        return secretList.stream()
                .map(secretListEntry -> {
                    final var name = secretListEntry.name();
                    final var description = Optional.ofNullable(secretListEntry.description()).orElse("");

                    // Return the secretListEntry, but apply formatting to the relevant fields
                    return secretListEntry.copy((builder) -> {
                        builder.name(nameFormatter.apply(name));
                        builder.description(descriptionFormatter.apply(description));
                    });
                })
                .flatMap(secretListEntry -> {
                    final var arn = secretListEntry.arn();
                    final var name = secretListEntry.name();
                    final var description = secretListEntry.description();
                    final var tags = Lists.toMap(secretListEntry.tags(), Tag::key, Tag::value);
                    final var cred = CredentialsFactory.create(arn, name, description, tags, client);
                    return cred.stream();
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

    private static SecretsManagerClient createClient(PluginConfiguration config) {
        final Client clientConfig = Optional.ofNullable(config.getClient())
                .orElse(new Client(null, null, null, null));

        return clientConfig.build();
    }
}
