package io.jenkins.plugins.credentials.secretsmanager;

import com.google.common.base.Suppliers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.common.IdCredentials;

import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Filters;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jenkins.model.GlobalConfiguration;

abstract class CredentialsSupplierFactory {

    private CredentialsSupplierFactory() {

    }

    static Supplier<Collection<IdCredentials>> create() {
        final Supplier<Collection<IdCredentials>> baseSupplier =
                new LazyAwsCredentialsSupplier(CredentialsSupplierFactory::getPluginConfiguration);
        return memoizeWithExpiration(baseSupplier, Duration.ofMinutes(5));
    }

    private static PluginConfiguration getPluginConfiguration() {
        return GlobalConfiguration.all().get(PluginConfiguration.class);
    }

    private static class LazyAwsCredentialsSupplier implements Supplier<Collection<IdCredentials>> {

        private static final Logger LOG = Logger.getLogger(
                LazyAwsCredentialsSupplier.class.getName());

        private final Supplier<PluginConfiguration> configurationSupplier;

        private LazyAwsCredentialsSupplier(Supplier<PluginConfiguration> configurationSupplier) {
            this.configurationSupplier = configurationSupplier;
        }

        @Override
        public Collection<IdCredentials> get() {
            final PluginConfiguration config = configurationSupplier.get();

            // secrets manager
            final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClient.builder();
            final EndpointConfiguration ec = config.getEndpointConfiguration();
            final AWSSecretsManager client;
            if (ec == null || (ec.getServiceEndpoint() == null || ec.getSigningRegion() == null)) {
                LOG.log(Level.CONFIG, "Default Endpoint Configuration");
                client = builder.build();
            } else {
                LOG.log(Level.CONFIG, "Custom Endpoint Configuration: {0}", ec);
                final AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                        new AwsClientBuilder.EndpointConfiguration(ec.getServiceEndpoint(),
                                                                   ec.getSigningRegion());
                client = builder.withEndpointConfiguration(endpointConfiguration).build();
            }

            Supplier<List<SecretListEntry>> strategy = new ListSecretsOperation(client);

            // tag filtering
            final Filters filters = config.getFilters();
            if (filters != null && filters.getTag() != null) {
                final String key = filters.getTag().getKey();
                final String value = filters.getTag().getValue();
                LOG.log(Level.CONFIG, "Custom tag filter: " + key + " = " + value);
                strategy = new ListSecretsFilter(strategy, new Tag().withKey(key).withValue(value));
            }

            return new AwsCredentialsSupplier(client, strategy).get();
        }
    }

    private static class AwsCredentialsSupplier implements Supplier<Collection<IdCredentials>> {

        private static final Logger LOG = Logger.getLogger(AwsCredentialsSupplier.class.getName());

        private final AWSSecretsManager client;
        private final Supplier<List<SecretListEntry>> strategy;

        AwsCredentialsSupplier(AWSSecretsManager client, Supplier<List<SecretListEntry>> strategy) {
            this.client = client;
            this.strategy = strategy;
        }

        @Override
        public Collection<IdCredentials> get() {
            LOG.log(Level.FINE,"Retrieve secrets from AWS Secrets Manager");

            final List<SecretListEntry> secretList = strategy.get();

            final ConcurrentHashMap<String, IdCredentials> credentials = new ConcurrentHashMap<>();
            for (SecretListEntry s : secretList) {
                final String name = s.getName();
                final String description = Optional.ofNullable(s.getDescription()).orElse("");
                final Map<String, String> tags = Optional.ofNullable(s.getTags()).orElse(Collections.emptyList())
                        .stream()
                        .filter(tag -> (tag.getKey() != null) && (tag.getValue() != null))
                        .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
                final IdCredentials cred = new AwsCredentials(name, description, tags, client);
                credentials.put(name, cred);
            }

            return credentials.values();
        }
    }

    private static class ListSecretsFilter implements Supplier<List<SecretListEntry>> {

        private final Supplier<List<SecretListEntry>> delegate;

        private final Tag tag;

        ListSecretsFilter(Supplier<List<SecretListEntry>> delegate, Tag tag) {
            this.delegate = delegate;
            this.tag = tag;
        }

        @Override
        public List<SecretListEntry> get() {
            final List<SecretListEntry> secrets = delegate.get();

            return secrets.stream()
                    .filter(s -> Optional.ofNullable(s.getTags()).orElse(Collections.emptyList())
                            .contains(tag))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Memoize a supplier function with expiration.
     *
     * @param <T> The return type of the supplier.
     * @param base The supplier to memoize.
     * @param duration The cache duration. Accurate to the nearest millisecond.
     * @return The supplier, memoized.
     */
    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Duration duration) {
        return Suppliers.memoizeWithExpiration(base::get, duration.toMillis(),
                TimeUnit.MILLISECONDS)::get;
    }
}
