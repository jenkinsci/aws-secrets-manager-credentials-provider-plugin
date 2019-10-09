package io.jenkins.plugins.credentials.secretsmanager;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class AwsSecretsManagerConfigTest {
    @Test
    public void shouldHandleDefaultEndpointConfiguration() {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();

        final AWSSecretsManagerClient expected = (AWSSecretsManagerClient) builder.build();
        final AWSSecretsManagerClient actual = (AWSSecretsManagerClient) AwsSecretsManagerConfig.fromBuilder(builder).build();

        assertSoftly(s -> {
           s.assertThat(actual.getEndpointPrefix()).as("Endpoint Prefix").isEqualTo(expected.getEndpointPrefix());
           s.assertThat(actual.getSignerRegionOverride()).as("Signer Region Override").isEqualTo(expected.getSignerRegionOverride());
        });
    }

    @Test
    public void shouldHandleCustomEndpointConfiguration() {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4584", "eu-west-1"));

        final AWSSecretsManagerClient expected = (AWSSecretsManagerClient) builder.build();
        final AWSSecretsManagerClient actual = (AWSSecretsManagerClient) AwsSecretsManagerConfig.fromBuilder(builder).build();

        assertSoftly(s -> {
            s.assertThat(actual.getEndpointPrefix()).as("Endpoint Prefix").isEqualTo(expected.getEndpointPrefix());
            s.assertThat(actual.getSignerRegionOverride()).as("Signer Region Override").isEqualTo(expected.getSignerRegionOverride());
        });
    }
}
