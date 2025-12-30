package io.jenkins.plugins.credentials.secretsmanager.util;

import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

/**
 * Wraps client-side access to AWS Secrets Manager in tests. Defers client initialization in case you want to set AWS
 * environment variables or Java properties in a wrapper Rule first.
 */
public class AWSSecretsManagerRule extends ExternalResource {

    private static final DockerImageName MOTO_IMAGE = DockerImageName.parse("motoserver/moto:5.1.18");

    private static final Region REGION = Region.US_EAST_1;

    private final GenericContainer<?> secretsManager = new GenericContainer<>(MOTO_IMAGE)
            .withExposedPorts(5000)
            .waitingFor(Wait.forHttp("/"));

    private transient SecretsManagerClient client;

    public String getEndpointUrl() {
        final String host = secretsManager.getHost();
        final int port = secretsManager.getFirstMappedPort();
        return String.format("http://%s:%d", host, port);
    }

    public String getRegion() {
        return REGION.toString();
    }

    public String getHost() {
        return secretsManager.getHost();
    }

    @Override
    public void before() {
        secretsManager.start();

        final String endpointUrl = getEndpointUrl();

        client = SecretsManagerClient.builder()
                .region(REGION)
                .endpointOverride(URI.create(endpointUrl))
                .build();
    }

    @Override
    protected void after() {
        client = null;
        secretsManager.stop();
    }

    public SecretsManagerClient getClient() {
        return client;
    }
}