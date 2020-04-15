package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import hudson.Extension;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.credentials.secretsmanager.config.EndpointConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension(optional = true)
public class AwsSecretSource extends SecretSource {

    private static final Logger LOG = Logger.getLogger(AwsSecretSource.class.getName());

    @Override
    public Optional<String> reveal(String id) throws IOException {
        final AWSSecretsManager client;
        try {
            client = createClient();
        } catch (SdkClientException e) {
            throw new IOException(e);
        }

        try {
            final GetSecretValueResult result = client.getSecretValue(new GetSecretValueRequest().withSecretId(id));

            if (result.getSecretBinary() != null) {
                throw new IOException(String.format("The binary secret '%s' is not supported. Please change its value to a string, or alternatively delete it.", result.getName()));
            }

            return Optional.ofNullable(result.getSecretString());
        } catch (ResourceNotFoundException e) {
            // Recoverable errors
            LOG.info(e.getMessage());
            return Optional.empty();
        } catch (AWSSecretsManagerException e) {
            // Unrecoverable errors
            throw new IOException(e);
        }
    }

    private AWSSecretsManager createClient() throws SdkClientException {
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
}
