package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import java.util.List;
import java.util.Optional;

public class PluginConfigurationForm {

    private final HtmlForm form;

    public PluginConfigurationForm(HtmlForm form) {
        this.form = form;
    }

    public Optional<String> getValidateSuccessMessage() {
        return form.getElementsByAttribute("div", "class", "ok")
                .stream()
                .map(DomNode::getTextContent)
                .filter(msg -> !msg.equalsIgnoreCase("Without a resource root URL, resources will be served from the Jenkins URL with Content-Security-Policy set."))
                .findFirst();
    }

    public String getValidateErrorMessage() {
        return form.getOneHtmlElementByAttribute("div", "class", "error").getTextContent();
    }

    public List<HtmlButton> getRepeatableAddButtons(String settingName) {
        return form.getByXPath(XPaths.repeatableAddButtons(settingName));
    }

    public HtmlButton getValidateButton(String textContent) {
        return form.getFirstByXPath(XPaths.validateButtons(textContent));
    }

    private static class XPaths {
        private static String validateButtons(String textContent) {
            return String.format("//span[contains(string(@class),'validate-button')]//button[contains(text(), '%s')]", textContent);
        }

        private static String repeatableAddButtons(String settingName) {
            return String.format("//td[contains(text(), '%s')]/following-sibling::td[@class='setting-main']//span[contains(string(@class),'repeatable-add')]//button[contains(text(), 'Add')]", settingName);
        }
    }
}
