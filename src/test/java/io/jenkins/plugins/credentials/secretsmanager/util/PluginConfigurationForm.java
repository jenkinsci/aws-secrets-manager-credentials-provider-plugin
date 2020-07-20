package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginConfigurationForm {

    private final HtmlForm form;

    public PluginConfigurationForm(HtmlForm form) {
        this.form = form;
    }

    public void clear() {
        this.clearEndpointConfiguration();
        this.clearFilters();
    }

    public void clearFilters() {
        form.getInputByName("_.filters").setChecked(false);
    }

    public void setFilter(String key, String value) {
        form.getInputByName("_.filters").setChecked(true);

        form.getInputByName("_.tag").setChecked(true);
        form.getInputByName("_.key").setValueAttribute(key);
        form.getInputByName("_.value").setValueAttribute(value);
    }

    public void clearEndpointConfiguration() {
        form.getInputByName("_.endpointConfiguration").setChecked(false);
    }

    public void setEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        form.getInputByName("_.endpointConfiguration").setChecked(true);
        form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
        form.getInputByName("_.signingRegion").setValueAttribute(signingRegion);
    }

    private Optional<String> getValidateSuccessMessage() {
        return form.getElementsByAttribute("div", "class", "ok")
                .stream()
                .map(DomNode::getTextContent)
                .filter(msg -> !msg.equalsIgnoreCase("Without a resource root URL, resources will be served from the main domain with Content-Security-Policy set."))
                .findFirst();
    }

    private String getValidateErrorMessage() {
        return form.getOneHtmlElementByAttribute("div", "class", "error").getTextContent();
    }

    private HtmlButton getValidateButton(String textContent) {
        return form.getByXPath("//span[contains(string(@class),'validate-button')]//button")
                .stream()
                .map(obj -> (HtmlButton) (obj))
                .filter(button -> button.getTextContent().equals(textContent))
                .collect(Collectors.toList())
                .get(0);
    }

    public FormValidationResult clickValidateButton(String textContent) {
        try {
            this.getValidateButton(textContent).click();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Optional<String> successMessage = this.getValidateSuccessMessage();
        if (successMessage.isPresent()) {
            return FormValidationResult.success(successMessage.get());
        } else {
            final String failureMessage = this.getValidateErrorMessage();
            return FormValidationResult.error(failureMessage);
        }
    }
}
