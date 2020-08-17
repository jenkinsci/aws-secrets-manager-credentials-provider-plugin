package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.FiltersFactory;
import io.jenkins.plugins.credentials.secretsmanager.config.*;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.Clients;
import io.jenkins.plugins.credentials.secretsmanager.config.Filters;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import io.jenkins.plugins.credentials.secretsmanager.factory.CredentialsFactory;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final List<io.jenkins.plugins.credentials.secretsmanager.config.Filter> filtersConfig = Optional.ofNullable(config.getListSecrets())
                .map(ListSecrets::getFilters)
                .orElse(Collections.emptyList());
        final Collection<Filter> filters = FiltersFactory.create(filtersConfig);

        final Optional<List<AWSSecretsManager>> clients = Optional.ofNullable(config.getBeta())
                .flatMap(beta -> Optional.ofNullable(beta.getClients()))
                .map(Clients::build);

        final Stream<StandardCredentials> creds;
        if (clients.isPresent()) {
            // Custom behavior
            final Collection<Supplier<Collection<StandardCredentials>>> multipleSuppliers = clients.get().stream()
                    .map(client -> new SingleAccountCredentialsSupplier(client, SecretListEntry::getARN, filters))
                    .collect(Collectors.toList());

            final ParallelSupplier<Collection<StandardCredentials>> supplier = new ParallelSupplier<>(multipleSuppliers);
            try {
                creds = supplier.get()
                        .stream()
                        .flatMap(Collection::stream);
            } catch (CompletionException | IllegalStateException e) {
                // Re-throw in a way that the provider knows how to catch
                throw new SdkBaseException(e.getCause());
            }
        } else {
            // Default behavior
            final Client clientConfig = new Client(new DefaultAWSCredentialsProviderChain(), config.getEndpointConfiguration(), null);
            final AWSSecretsManager secretsManager = clientConfig.build();
            final SingleAccountCredentialsSupplier supplier = new SingleAccountCredentialsSupplier(secretsManager, SecretListEntry::getName, filters);
            creds = supplier.get().stream();
        }

        return creds
                .collect(Collectors.toMap(StandardCredentials::getId, Function.identity()))
                .values();
    }

    private static class SingleAccountCredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

        private final AWSSecretsManager client;
        private final Function<SecretListEntry, String> nameSelector;
        private final Collection<Filter> filters;

        SingleAccountCredentialsSupplier(AWSSecretsManager client, Function<SecretListEntry, String> nameSelector, Collection<Filter> filters) {
            this.client = client;
            this.nameSelector = nameSelector;
            this.filters = filters;
        }

        @Override
        public Collection<StandardCredentials> get() {
            final Collection<SecretListEntry> secretList = new ListSecretsOperation(client, filters).get();

            return secretList.stream()
                    .flatMap(secretListEntry -> {
                        final String name = nameSelector.apply(secretListEntry);
                        final String description = Optional.ofNullable(secretListEntry.getDescription()).orElse("");
                        final Map<String, String> tags = Lists.toMap(secretListEntry.getTags(), Tag::getKey, Tag::getValue);
                        final Optional<StandardCredentials> cred = CredentialsFactory.create(name, description, tags, client);
                        return Optionals.stream(cred);
                    })
                    .collect(Collectors.toList());
        }
    }
}
