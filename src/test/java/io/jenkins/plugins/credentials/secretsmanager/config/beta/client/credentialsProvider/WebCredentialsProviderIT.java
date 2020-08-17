package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.credentialsProvider;

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
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.setClientWithDefaultAWSCredentialsProviderChain();
        });
    }

    @Override
    protected void setCredentialsProvider(String roleArn, String roleSessionName) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.setClientWithSTSAssumeRoleSessionCredentialsProvider(roleArn, roleSessionName);
        });
    }

    @Override
    protected void setCredentialsProvider(String profileName) {
        r.configure(f -> {
            final PluginConfigurationForm form = new PluginConfigurationForm(f);
            form.setClientWithProfileCredentialsProvider(profileName);
        });
    }
}
