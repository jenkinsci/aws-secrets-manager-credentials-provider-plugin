package io.jenkins.plugins.credentials.secretsmanager.config.client.credentialsProvider;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CasCCredentialsProviderIT extends AbstractCredentialsProviderIT {

    @Rule
    public final JenkinsRule r = new JenkinsConfiguredWithCodeRule();

    @Override
    protected PluginConfiguration getPluginConfiguration() {
        return (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    }

    @Override
    protected void setCredentialsProvider() {
        // no-op (configured by annotations)
    }

    @Override
    protected void setCredentialsProvider(String roleArn, String roleSessionName) {
        // no-op (configured by annotations)
    }

    @Override
    protected void setCredentialsProvider(String profileName) {
        // no-op (configured by annotations)
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/client/credentialsProvider/default.yml")
    public void shouldSupportDefault() {
        super.shouldSupportDefault();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/client/credentialsProvider/assumeRole.yml")
    public void shouldSupportAssumeRole() {
        super.shouldSupportAssumeRole();
    }

    @Override
    @Test
    @ConfiguredWithCode("/config/client/credentialsProvider/profile.yml")
    public void shouldSupportProfile() {
        super.shouldSupportProfile();
    }
}
