package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.SdkBaseException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.Clients;
import io.jenkins.plugins.credentials.secretsmanager.config.Filters;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import io.jenkins.plugins.credentials.secretsmanager.factory.CredentialsFactory;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;
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

        final Filters filters = config.getFilters();
        final Predicate<SecretListEntry> secretFilter = newSecretFilter(filters);

        final Optional<List<AWSSecretsManager>> clients = Optional.ofNullable(config.getBeta())
                .flatMap(beta -> Optional.ofNullable(beta.getClients()))
                .map(Clients::build);

        final Stream<StandardCredentials> creds;
        if (clients.isPresent()) {
            // Custom behavior
            final Collection<Supplier<Collection<StandardCredentials>>> multipleSuppliers = clients.get().stream()
                    .map(client -> new SingleAccountCredentialsSupplier(client, SecretListEntry::getARN, secretFilter))
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
            final SingleAccountCredentialsSupplier supplier = new SingleAccountCredentialsSupplier(secretsManager, SecretListEntry::getName, secretFilter);
            creds = supplier.get().stream();
        }

        return creds
                .collect(Collectors.toMap(StandardCredentials::getId, Function.identity()))
                .values();
    }

    private static Predicate<SecretListEntry> newSecretFilter(Filters filters) {
        if (filters != null && filters.getTag() != null) {
            final com.amazonaws.services.secretsmanager.model.Tag filterTag = new Tag()
                    .withKey(filters.getTag().getKey())
                    .withValue(filters.getTag().getValue());
            return s -> Optional.ofNullable(s.getTags()).orElse(Collections.emptyList()).contains(filterTag);
        } else {
            return s -> true;
        }
    }

    private static class SingleAccountCredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

        private final AWSSecretsManager client;
        private final Function<SecretListEntry, String> nameSelector;
        private final Predicate<SecretListEntry> secretFilter;

        SingleAccountCredentialsSupplier(AWSSecretsManager client, Function<SecretListEntry, String> nameSelector, Predicate<SecretListEntry> secretFilter) {
            this.client = client;
            this.nameSelector = nameSelector;
            this.secretFilter = secretFilter;
        }

        @Override
        public Collection<StandardCredentials> get() {
            final Collection<SecretListEntry> secretList = new ListSecretsOperation(client).get();

            return secretList.stream()
                    .filter(secretFilter)
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
