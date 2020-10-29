package io.jenkins.plugins.credentials.secretsmanager.util;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import java.util.List;

public class PluginConfigurationForm {

    private final HtmlForm form;

    public PluginConfigurationForm(HtmlForm form) {
        this.form = form;
    }

    public List<HtmlButton> getRepeatableAddButtons(String settingName) {
        return form.getByXPath(XPaths.repeatableAddButtons(settingName));
    }

    public HtmlSelect getDropdownList(String settingName) {
        final String dropdownList = XPaths.dropdownList(settingName);
        return form.getFirstByXPath(dropdownList);
    }

    private static class XPaths {
        private static String dropdownList(String settingName) {
            return String.format("//div[contains(@class, 'setting-name') and normalize-space(text()) = '%s']/following-sibling::div[contains(@class, 'setting-main')]/select[contains(@class, 'dropdownList')]", settingName);
        }

        private static String repeatableAddButtons(String settingName) {
            return String.format("//div[contains(@class, 'setting-name') and text()='%s']/following-sibling::div[@class='setting-main']//span[contains(string(@class),'repeatable-add')]//button[contains(text(), 'Add')]", settingName);
        }
    }
}
