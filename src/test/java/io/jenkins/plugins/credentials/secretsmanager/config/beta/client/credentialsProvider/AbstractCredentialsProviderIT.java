package io.jenkins.plugins.credentials.secretsmanager.config.beta.client.credentialsProvider;

import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.ProfileCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.STSAssumeRoleSessionCredentialsProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCredentialsProviderIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setCredentialsProvider();

    protected abstract void setCredentialsProvider(String roleArn, String roleSessionName);

    protected abstract void setCredentialsProvider(String profileName);

    @Test
    public void shouldSupportDefault() {
        // Given
        setCredentialsProvider();

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getBeta().getClients().getClients())
                .extracting("credentialsProvider")
                .containsOnly(new DefaultAWSCredentialsProviderChain());
    }

    @Test
    public void shouldSupportAssumeRole() {
        // Given
        final String roleArn = "arn:aws:iam::111111111111:role/foo-role";
        final String roleSessionName = "foo";
        setCredentialsProvider(roleArn, roleSessionName);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getBeta().getClients().getClients())
                .extracting("credentialsProvider")
                .containsOnly(new STSAssumeRoleSessionCredentialsProvider(roleArn, roleSessionName));
    }

    @Test
    public void shouldSupportProfile() {
        // Given
        final String profileName = "foo";
        setCredentialsProvider(profileName);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getBeta().getClients().getClients())
                .extracting("credentialsProvider")
                .containsOnly(new ProfileCredentialsProvider(profileName));
    }
}
