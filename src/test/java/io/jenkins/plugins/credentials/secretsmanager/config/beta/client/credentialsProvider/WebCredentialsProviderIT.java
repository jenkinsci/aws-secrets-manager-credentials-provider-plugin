package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.credentialsProvider;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.util.JenkinsConfiguredWithWebRule;
import io.jenkins.plugins.credentials.secretsmanager.util.PluginConfigurationForm;
import org.junit.Rule;

public class WebCredentialsProviderIT extends AbstractCredentialsProviderIT {

    @Rule
    public final JenkinsConfiguredWithWebRule r = new JenkinsConfiguredWithWebRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setCredentialsProvider() {
        r.configure(f ->
                EnhancedForm.decorate(f).setClientWithDefaultAWSCredentialsProviderChain());
    }

    @Override
    protected void setCredentialsProvider(String roleArn, String roleSessionName) {
        r.configure(f ->
                EnhancedForm.decorate(f).setClientWithSTSAssumeRoleSessionCredentialsProvider(roleArn, roleSessionName));
    }

    @Override
    protected void setCredentialsProvider(String profileName) {
        r.configure(f ->
                EnhancedForm.decorate(f).setClientWithProfileCredentialsProvider(profileName));
    }

    private static class EnhancedForm {

        private final HtmlForm form;

        private EnhancedForm(HtmlForm form) {
            this.form = form;
        }

        public static EnhancedForm decorate(HtmlForm form) {
            return new EnhancedForm(form);
        }

        public void setClientWithDefaultAWSCredentialsProviderChain() {
            setBeta(true);
            setClients(true);
            setClientCredentialsProviderSelect("Default");
        }

        public void setClientWithProfileCredentialsProvider(String profileName) {
            setBeta(true);
            setClients(true);
            setClientCredentialsProviderSelect("Profile");
            form.getInputByName("_.profileName").setValueAttribute(profileName);
        }

        public void setClientWithSTSAssumeRoleSessionCredentialsProvider(String roleArn, String roleSessionName) {
            setBeta(true);
            setClients(true);
            setClientCredentialsProviderSelect("STS AssumeRole");
            form.getInputByName("_.roleArn").setValueAttribute(roleArn);
            form.getInputByName("_.roleSessionName").setValueAttribute(roleSessionName);
        }

        private void setBeta(boolean checked) {
            form.getInputByName("_.beta").setChecked(checked);
        }

        private void setClients(boolean checked) {
            form.getInputByName("_.clients").setChecked(checked);
        }

        private void setClientCredentialsProviderSelect(String optionText) {
            final HtmlSelect select = (HtmlSelect) form.getByXPath("//div[contains(string(@name), 'clients')]//select[contains(string(@class),'dropdownList')]").get(0);
            select.getOptionByText(optionText).setSelected(true);
        }
    }
}
