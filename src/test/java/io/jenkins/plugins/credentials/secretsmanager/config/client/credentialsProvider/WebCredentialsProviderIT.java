package io.jenkins.plugins.credentials.secretsmanager.config.client.credentialsProvider;

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
        r.configure(form -> {
            setClientCredentialsProviderSelect(form,"Default");
        });
    }

    @Override
    protected void setCredentialsProvider(String roleArn, String roleSessionName) {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form,"STS AssumeRole");
            form.getInputByName("_.roleArn").setValueAttribute(roleArn);
            form.getInputByName("_.roleSessionName").setValueAttribute(roleSessionName);
        });
    }

    @Override
    protected void setCredentialsProvider(String profileName) {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form, "Profile");
            form.getInputByName("_.profileName").setValueAttribute(profileName);
        });
    }

    private void setClientCredentialsProviderSelect(HtmlForm form, String optionText) {
        final PluginConfigurationForm pluginConfigurationForm = new PluginConfigurationForm(form);
        final HtmlSelect select = pluginConfigurationForm.getDropdownList("Credentials Provider");
        select.getOptionByText(optionText).setSelected(true);
    }
}
