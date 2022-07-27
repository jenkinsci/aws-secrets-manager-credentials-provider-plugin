package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Wraps client-side access to AWS Secrets Manager in tests. Defers client initialization in case you want to set AWS
 * environment variables or Java properties in a wrapper Rule first.
 */
public class AWSSecretsManagerRule extends ExternalResource {

    private static final DockerImageName MOTO_IMAGE = DockerImageName.parse("motoserver/moto:2.3.0");

    private static final String SIGNING_REGION = "us-east-1";

    private final GenericContainer<?> secretsManager = new GenericContainer<>(MOTO_IMAGE)
            .withExposedPorts(5000)
            .waitingFor(Wait.forHttp("/"));

    private transient AWSSecretsManager client;

    public String getServiceEndpoint() {
        final String host = secretsManager.getHost();
        final int port = secretsManager.getFirstMappedPort();
        return String.format("http://%s:%d", host, port);
    }

    public String getSigningRegion() {
        return SIGNING_REGION;
    }

    public String getHost() {
        return secretsManager.getHost();
    }

    @Override
    public void before() {
        secretsManager.start();

        final String serviceEndpoint = getServiceEndpoint();

        client = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, SIGNING_REGION))
                .build();
    }

    @Override
    protected void after() {
        client = null;
        secretsManager.stop();
    }

    public AWSSecretsManager getClient() {
        return client;
    }
}