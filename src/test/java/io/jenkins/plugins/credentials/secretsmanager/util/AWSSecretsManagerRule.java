package io.jenkins.plugins.credentials.secretsmanager.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * Wraps client-side access to AWS Secrets Manager in tests. Defers client initialization in case you want to set AWS
 * environment variables or Java properties in a wrapper Rule first.
 */
public class AWSSecretsManagerRule extends ExternalResource {

    private static final ImageFromDockerfile MOTO_IMAGE = new ImageFromDockerfile()
            .withDockerfileFromBuilder(b ->
                    b.from("python:3.7")
                            .run("pip install moto[server]==2.3.0")
                            .cmd("moto_server -H 0.0.0.0 -p 4584")
                            .build());

    private static final String SIGNING_REGION = "us-east-1";

    private final GenericContainer<?> secretsManager = new GenericContainer<>(MOTO_IMAGE)
            .withExposedPorts(4584)
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