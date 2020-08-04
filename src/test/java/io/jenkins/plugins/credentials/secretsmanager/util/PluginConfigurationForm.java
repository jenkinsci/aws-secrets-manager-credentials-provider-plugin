package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import java.io.IOException;
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
        this.clearClients();
    }

    public void clearFilters() {
        form.getInputByName("_.filters").setChecked(false);
    }

    public void setFilter(String key, String value) {
        form.getInputByName("_.filters").setChecked(true);

        form.getInputByName("_.tag").setChecked(true);
        form.getInputByName("_.key").setValueAttribute(key);
        // The Jenkins config form HTML is not hierarchical, necessitating this fragile selector.
        form.getInputsByName("_.value").stream()
                .reduce((first, second) -> second)
                .ifPresent(lastValueInputInForm -> lastValueInputInForm.setValueAttribute(value));
    }

    public void clearClients() {
        form.getInputByName("_.clients").setChecked(false);
    }

    public void setClientWithRegion(String region) {
        form.getInputByName("_.beta").setChecked(true);
        form.getInputByName("_.clients").setChecked(true);
        form.getInputByName("_.region").setChecked(true);
        form.getInputsByName("_.region").get(1).setValueAttribute(region);
    }

    public void setClientWithEndpointConfiguration(String serviceEndpoint, String signingRegion) {
        form.getInputByName("_.beta").setChecked(true);
        form.getInputByName("_.clients").setChecked(true);
        form.getInputByName("_.endpointConfiguration").setChecked(true);
        form.getInputByName("_.serviceEndpoint").setValueAttribute(serviceEndpoint);
        form.getInputByName("_.signingRegion").setValueAttribute(signingRegion);
    }

    public void setClientWithDefaultAWSCredentialsProviderChain() {
        form.getInputByName("_.beta").setChecked(true);
        form.getInputByName("_.clients").setChecked(true);
        setClientCredentialsProviderSelect("Default");
    }

    public void setClientWithProfileCredentialsProvider(String profileName) {
        form.getInputByName("_.beta").setChecked(true);
        form.getInputByName("_.clients").setChecked(true);
        setClientCredentialsProviderSelect("Profile");
        form.getInputByName("_.profileName").setValueAttribute(profileName);
    }

    public void setClientWithSTSAssumeRoleSessionCredentialsProvider(String roleArn, String roleSessionName) {
        form.getInputByName("_.beta").setChecked(true);
        form.getInputByName("_.clients").setChecked(true);
        setClientCredentialsProviderSelect("STS AssumeRole");
        form.getInputByName("_.roleArn").setValueAttribute(roleArn);
        form.getInputByName("_.roleSessionName").setValueAttribute(roleSessionName);
    }

    private void setClientCredentialsProviderSelect(String optionText) {
        final HtmlSelect select = (HtmlSelect) form.getByXPath("//div[contains(string(@name), 'clients')]//select[contains(string(@class),'dropdownList')]").get(0);
        select.getOptionByText(optionText).setSelected(true);
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
