package io.jenkins.plugins.credentials.secretsmanager;

import com.google.common.base.Suppliers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;

import org.acegisecurity.Authentication;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Filters;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import jenkins.model.Jenkins;

@Extension
public class AwsCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AwsCredentialsProvider.class.getName());

    private final AwsCredentialsStore store = new AwsCredentialsStore(this);

    private final Supplier<Collection<IdCredentials>> credentialsSupplier =
            memoizeWithExpiration(AwsCredentialsProvider::fetchCredentials, Duration.ofMinutes(5));

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          ItemGroup itemGroup,
                                                          Authentication authentication) {
        if (ACL.SYSTEM.equals(authentication)) {
            final ArrayList<C> list = new ArrayList<>();
            for (IdCredentials credential : credentialsSupplier.get()) {
                if (type.isAssignableFrom(credential.getClass())) {
                    // cast to keep generics happy even though we are assignable..
                    list.add(type.cast(credential));
                }
                LOG.log(Level.FINEST, "getCredentials {0} does not match", credential.getId());
            }
            return list;
        }

        return Collections.emptyList();
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        return object == Jenkins.getInstance() ? store : null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }

    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Duration duration) {
        return Suppliers.memoizeWithExpiration(base::get, duration.toMillis(), TimeUnit.MILLISECONDS)::get;
    }

    private static Collection<IdCredentials> fetchCredentials() {
        LOG.log(Level.FINE,"Retrieve secrets from AWS Secrets Manager");

        final PluginConfiguration config = PluginConfiguration.getInstance();
        final EndpointConfiguration ec = config.getEndpointConfiguration();
        final Filters filters = config.getFilters();

        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClient.builder();
        if (ec == null || (ec.getServiceEndpoint() == null || ec.getSigningRegion() == null)) {
            LOG.log(Level.CONFIG, "Default Endpoint Configuration");
        } else {
            LOG.log(Level.CONFIG, "Custom Endpoint Configuration: {0}", ec);
            final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(ec.getServiceEndpoint(), ec.getSigningRegion());
            builder.setEndpointConfiguration(endpointConfiguration);
        }
        final AWSSecretsManager client = builder.build();

        final Predicate<SecretListEntry> secretFilter;
        if (filters != null && filters.getTag() != null) {
            final Tag filterTag = new Tag().withKey(filters.getTag().getKey()).withValue(filters.getTag().getValue());
            secretFilter = s -> Optional.ofNullable(s.getTags()).orElse(Collections.emptyList()).contains(filterTag);
        } else {
            secretFilter = s -> true;
        }

        AwsCredentialsFactory credFactory = new AwsCredentialsFactory();

        final Map<String, IdCredentials> credentials = new ListSecretsOperation(client).get().stream()
                .filter(secretFilter)
                .map(s -> {
                    final String name = s.getName();
                    final String description = Optional.ofNullable(s.getDescription()).orElse("");
                    final Map<String, String> tags = Optional.ofNullable(s.getTags()).orElse(Collections.emptyList()).stream()
                            .filter(tag -> (tag.getKey() != null) && (tag.getValue() != null))
                            .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
                    return credFactory.create(name, description, tags, client);
                })
                .collect(Collectors.toMap(IdCredentials::getId, cred -> cred));

        return credentials.values();
    }
}
