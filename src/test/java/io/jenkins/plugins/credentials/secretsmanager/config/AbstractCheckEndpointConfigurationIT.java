package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.credentials.secretsmanager.util.FormValidationResult;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public abstract class AbstractCheckEndpointConfigurationIT {

    protected abstract FormValidationResult validate(String serviceEndpoint, String signingRegion);

    @Test
    public void shouldAllowGoodEndpointConfiguration() {
        // When
        final FormValidationResult result = validate("http://localhost:4584", "us-east-1");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isTrue();
            s.assertThat(result.getMessage()).as("Message").isEqualTo("Success");
        });
    }

    @Test
    public void shouldRejectBadEndpointConfiguration() {
        // When
        final FormValidationResult result = validate("http://localhost:1", "us-east-1");

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isFalse();
            s.assertThat(result.getMessage()).as("Message").startsWith("AWS client error");
        });
    }
}
