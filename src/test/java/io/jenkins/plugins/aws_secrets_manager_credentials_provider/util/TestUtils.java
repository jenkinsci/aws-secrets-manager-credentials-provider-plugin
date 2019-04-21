package io.jenkins.plugins.aws_secrets_manager_credentials_provider.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

/**
 * Mimic just the bit of Localstack that we are interested in.
 */
public class TestUtils {

    private static final AwsClientBuilder.EndpointConfiguration TEST_ENDPOINT_CONFIGURATION =
            new AwsClientBuilder.EndpointConfiguration("http://localhost:4584", "us-east-1");

    private static final AWSCredentialsProvider TEST_CREDENTIALS_PROVIDER =
            new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test"));

    public static AWSSecretsManager getClientSecretsManager() {
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(TEST_ENDPOINT_CONFIGURATION)
                .withCredentials(TEST_CREDENTIALS_PROVIDER)
                .build();
    }
}
