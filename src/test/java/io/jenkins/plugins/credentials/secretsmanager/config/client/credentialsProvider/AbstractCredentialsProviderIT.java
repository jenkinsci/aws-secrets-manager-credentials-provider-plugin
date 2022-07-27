package io.jenkins.plugins.credentials.secretsmanager.config.client.credentialsProvider;

import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.ProfileCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.STSAssumeRoleSessionCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions;
import org.junit.Test;

import java.util.Optional;

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

        // Then (it's allowed to be null or an instance of the default type)
        CustomAssertions.assertThat(Optional.ofNullable(config).map(PluginConfiguration::getClient).map(Client::getCredentialsProvider))
                .isEmptyOrContains(new DefaultAWSCredentialsProviderChain());
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
        assertThat(config.getClient().getCredentialsProvider())
                .isEqualTo(new STSAssumeRoleSessionCredentialsProvider(roleArn, roleSessionName));
    }

    @Test
    public void shouldSupportProfile() {
        // Given
        final String profileName = "foo";
        setCredentialsProvider(profileName);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getCredentialsProvider())
                .isEqualTo(new ProfileCredentialsProvider(profileName));
    }
}
