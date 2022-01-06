package io.jenkins.plugins.credentials.secretsmanager.config.endpointConfiguration;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import io.jenkins.plugins.credentials.secretsmanager.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CheckEndpointConfigurationIT {

    public final JenkinsConfiguredWithWebRule jenkins = new JenkinsConfiguredWithWebRule();

    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    public void shouldAllowGoodEndpointConfiguration() {
        // When
        final FormValidationResult result = validate(secretsManager.getServiceEndpoint(), secretsManager.getSigningRegion());

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isTrue();
            s.assertThat(result.getMessage()).as("Message").isEqualTo("Success");
        });
    }

    @Test
    public void shouldRejectBadEndpointConfiguration() {
        // When
        final String badServiceEndpoint = String.format("http://%s:0", secretsManager.getHost());
        final FormValidationResult result = validate(badServiceEndpoint, secretsManager.getSigningRegion());

        // Then
        assertSoftly(s -> {
            s.assertThat(result.isSuccess()).as("Success").isFalse();
            s.assertThat(result.getMessage()).as("Message").startsWith("AWS client error");
        });
    }

    private FormValidationResult validate(String serviceEndpoint, String signingRegion) {
        final AtomicReference<FormValidationResult> result = new AtomicReference<>();

        jenkins.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);

            f.getInputByName("_.endpointConfiguration").setChecked(true);
            f.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
            f.getInputByName("_.signingRegion").setValueAttribute(signingRegion);

            final HtmlButton validateButton = form.getValidateButton("Test Endpoint Configuration");
            try {
                validateButton.click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final FormValidationResult r;
            final Optional<String> successMessage = form.getValidateSuccessMessage();
            if (successMessage.isPresent()) {
                r = FormValidationResult.success(successMessage.get());
            } else {
                final String failureMessage = form.getValidateErrorMessage();
                r = FormValidationResult.error(failureMessage);
            }

            result.set(r);
        });

        return result.get();
    }
}
