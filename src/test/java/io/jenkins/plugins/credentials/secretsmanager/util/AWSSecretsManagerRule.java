package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.junit.rules.ExternalResource;

/**
 * Wraps client-side access to AWS Secrets Manager in tests. Defers client initialization in case you want to set AWS
 * environment variables or Java properties in a wrapper Rule first.
 */
public class AWSSecretsManagerRule extends ExternalResource {

    private transient AWSSecretsManager client;

    @Override
    public void before() {
        client = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4584", "us-east-1"))
                .build();
    }

    @Override
    protected void after() {
        client = null;
    }

    public AWSSecretsManager getClient() {
        return client;
    }
}
