package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import hudson.Extension;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class AwsSecretSource extends SecretSource {

    private static final Logger LOG = Logger.getLogger(AwsSecretSource.class.getName());

    @Override
    public Optional<String> reveal(String id) throws IOException {
        final AWSSecretsManager client = createClient();

        try {
            final DescribeSecretResult describeSecretResult = client.describeSecret(new DescribeSecretRequest().withSecretId(id));

            if (softDeletionFilter.test(describeSecretResult)) {
                final GetSecretValueResult result = client.getSecretValue(new GetSecretValueRequest().withSecretId(id));
                return Optional.ofNullable(result.getSecretString());
            } else {
                LOG.info("Secret " + id + " has been soft-deleted");
                return Optional.empty();
            }
        } catch (AWSSecretsManagerException e) {
            throw new IOException(e);
        }
    }

    private AWSSecretsManager createClient() {
        final PluginConfiguration config = PluginConfiguration.getInstance();
        final EndpointConfiguration ec = config.getEndpointConfiguration();

        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClient.builder();
        if (ec == null || (ec.getServiceEndpoint() == null || ec.getSigningRegion() == null)) {
            LOG.log(Level.CONFIG, "Default Endpoint Configuration");
        } else {
            LOG.log(Level.CONFIG, "Custom Endpoint Configuration: {0}", ec);
            final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(ec.getServiceEndpoint(), ec.getSigningRegion());
            builder.setEndpointConfiguration(endpointConfiguration);
        }
        return builder.build();
    }

    private static Predicate<DescribeSecretResult> softDeletionFilter = (s) -> s.getDeletedDate() == null;
}
