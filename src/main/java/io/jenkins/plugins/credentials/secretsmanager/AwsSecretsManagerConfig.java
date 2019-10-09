package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

import java.io.Serializable;

/**
 * transportable client config structure
 */
class AwsSecretsManagerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String serviceEndpoint;
    private final String signingRegion;

    private AwsSecretsManagerConfig(String serviceEndpoint, String signingRegion) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
    }

    static AwsSecretsManagerConfig fromBuilder(AWSSecretsManagerClientBuilder b) {
        final AwsClientBuilder.EndpointConfiguration ec = b.getEndpoint();

        final String serviceEndpoint;
        final String signingRegion;

        if (ec == null) {
            serviceEndpoint = null;
            signingRegion = null;
        } else {
            serviceEndpoint = b.getEndpoint().getServiceEndpoint();
            signingRegion = b.getEndpoint().getSigningRegion();
        }
        return new AwsSecretsManagerConfig(serviceEndpoint, signingRegion);
    }

    AWSSecretsManager build() {
        final AWSSecretsManagerClientBuilder b = AWSSecretsManagerClientBuilder.standard();

        if (serviceEndpoint != null && signingRegion != null) {
            b.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion));
        }

        return b.build();
    }
}
