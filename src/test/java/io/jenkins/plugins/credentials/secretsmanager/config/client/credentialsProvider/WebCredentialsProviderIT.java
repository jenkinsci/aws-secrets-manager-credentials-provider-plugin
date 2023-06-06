package io.jenkins.plugins.credentials.secretsmanager.config.client.credentialsProvider;

import org.htmlunit.html.HtmlForm;
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
    protected void setDefaultCredentialsProvider() {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form,"Default");
        });
    }

    @Override
    protected void setSTSAssumeRoleCredentialsProvider(String roleArn, String roleSessionName) {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form,"STS AssumeRole");
            form.getInputByName("_.roleArn").setValue(roleArn);
            form.getInputByName("_.roleSessionName").setValue(roleSessionName);
        });
    }

    @Override
    protected void setProfileCredentialsProvider(String profileName) {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form, "Profile");
            form.getInputByName("_.profileName").setValue(profileName);
        });
    }

    @Override
    protected void setStaticCredentialsProvider(String accessKey, String secretKey) {
        r.configure(form -> {
            setClientCredentialsProviderSelect(form, "Static");
            form.getInputByName("_.accessKey").setValue(accessKey);
            form.getInputByName("_.secretKey").setValue(secretKey);
        });
    }

    private void setClientCredentialsProviderSelect(HtmlForm form, String optionText) {
        final var pluginConfigurationForm = new PluginConfigurationForm(form);
        final var select = pluginConfigurationForm.getDropdownList("Credentials Provider");
        select.getOptionByText(optionText).setSelected(true);
    }
}
