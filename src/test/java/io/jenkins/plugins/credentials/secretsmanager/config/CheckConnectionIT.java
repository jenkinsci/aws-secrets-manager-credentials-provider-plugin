package io.jenkins.plugins.credentials.secretsmanager.config;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;

import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;

import static org.assertj.core.api.Assertions.assertThat;

// NOTE: We can't call this TestConnectionIT as the 'Test' bit of the name gets matched by the unit-test runner.
public class CheckConnectionIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @BeforeClass
    public static void fakeAwsCredentials() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretKey", "test");
    }

    @Test
    public void shouldTestConnection() {
        r.configure(form -> {
            // Given
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute("http://localhost:4584");
            form.getInputByName("_.signingRegion").setValueAttribute("us-east-1");

            // When
            try {
                getValidateButton(form).click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Then
            assertThat(form.getOneHtmlElementByAttribute("div", "class", "ok").getTextContent()).isEqualTo("Success");
        });
    }

    @Test
    public void shouldRevealClientErrorsInTestConnection() {
        r.configure(form -> {
            // Given
            form.getInputByName("_.endpointConfiguration").setChecked(true);
            form.getInputByName("_.serviceEndpoint").setValueAttribute("http://localhost:1");
            form.getInputByName("_.signingRegion").setValueAttribute("us-east-1");

            // When
            try {
                getValidateButton(form).click();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Then
            assertThat(form.getOneHtmlElementByAttribute("div", "class", "error").getTextContent()).startsWith("AWS client error");
        });
    }

    private static HtmlButton getValidateButton(DomNode node) {
        return node.getByXPath("//span[contains(string(@class),'validate-button')]//button")
                .stream()
                .map(obj -> (HtmlButton) (obj))
                .filter(button -> button.getTextContent().equals("Test Connection"))
                .collect(Collectors.toList())
                .get(0);
    }
}
