package io.jenkins.plugins.credentials.secretsmanager.supplier;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.AssumeRoleDefaults;
import io.jenkins.plugins.credentials.secretsmanager.config.*;
import io.jenkins.plugins.credentials.secretsmanager.factory.CredentialsFactory;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;
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

        final EndpointConfiguration ec = config.getEndpointConfiguration();
        final AwsClientBuilder.EndpointConfiguration endpointConfiguration = newEndpointConfiguration(ec);

        final Filters filters = config.getFilters();
        final Predicate<SecretListEntry> secretFilter = newSecretFilter(filters);

        final List<Client> clients = Optional.ofNullable(config.getBeta())
                .flatMap(beta -> Optional.ofNullable(beta.getClients()))
                .map(c -> c.getClients())
                .orElse(Collections.emptyList());

        final Supplier<Collection<StandardCredentials>> mainSupplier =
                new SingleAccountCredentialsSupplier(newClient(endpointConfiguration), SecretListEntry::getName, secretFilter);

        final Collection<Supplier<Collection<StandardCredentials>>> otherSuppliers = clients.stream()
                .map(clientConfig -> {
                    final String role = clientConfig.getRole();
                    final AwsClientBuilder.EndpointConfiguration e = newEndpointConfiguration(clientConfig.getEndpointConfiguration()); // maybe null
                    final AWSSecretsManager secretsManager = newClient(role, e);
                    return new SingleAccountCredentialsSupplier(secretsManager, SecretListEntry::getARN, secretFilter);
                })
                .collect(Collectors.toList());

        final ParallelSupplier<Collection<StandardCredentials>> allSuppliers = new ParallelSupplier<>(Lists.concat(mainSupplier, otherSuppliers));

        try {
            return allSuppliers.get()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(StandardCredentials::getId, Function.identity()))
                    .values();
        } catch (CompletionException | IllegalStateException e) {
            // Re-throw in a way that the provider knows how to catch
            throw new SdkBaseException(e.getCause());
        }
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

    private static AWSSecretsManager newClient(AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .build();
    }

    private static AWSSecretsManager newClient(String roleArn, AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        final AWSCredentialsProvider roleCredentials = new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, AssumeRoleDefaults.SESSION_NAME)
                .withRoleSessionDurationSeconds(AssumeRoleDefaults.SESSION_DURATION_SECONDS)
                .build();

        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(roleCredentials)
                .build();
    }

    private static AwsClientBuilder.EndpointConfiguration newEndpointConfiguration(EndpointConfiguration ec) {
        if (ec == null || (ec.getServiceEndpoint() == null || ec.getSigningRegion() == null)) {
            return null;
        } else {
            return new AwsClientBuilder.EndpointConfiguration(ec.getServiceEndpoint(), ec.getSigningRegion());
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
