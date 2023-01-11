package io.jenkins.plugins.credentials.secretsmanager.config.client.credentialsProvider;

import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.AWSStaticCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.ProfileCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.STSAssumeRoleSessionCredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.util.assertions.CustomAssertions;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCredentialsProviderIT {

    protected abstract PluginConfiguration getPluginConfiguration();

    protected abstract void setDefaultCredentialsProvider();

    protected abstract void setSTSAssumeRoleCredentialsProvider(String roleArn, String roleSessionName);

    protected abstract void setProfileCredentialsProvider(String profileName);

    protected abstract void setStaticCredentialsProvider(String accessKey, String secretKey);

    @Test
    public void shouldSupportDefault() {
        // Given
        setDefaultCredentialsProvider();

        // When
        final var config = getPluginConfiguration();

        // Then (it's allowed to be null or an instance of the default type)
        CustomAssertions.assertThat(Optional.ofNullable(config).map(PluginConfiguration::getClient).map(Client::getCredentialsProvider))
                .isEmptyOrContains(new DefaultAWSCredentialsProviderChain());
    }

    @Test
    public void shouldSupportAssumeRole() {
        // Given
        final var roleArn = "arn:aws:iam::111111111111:role/foo-role";
        final var roleSessionName = "foo";
        setSTSAssumeRoleCredentialsProvider(roleArn, roleSessionName);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getCredentialsProvider())
                .isEqualTo(new STSAssumeRoleSessionCredentialsProvider(roleArn, roleSessionName));
    }

    @Test
    public void shouldSupportProfile() {
        // Given
        final var profileName = "foo";
        setProfileCredentialsProvider(profileName);

        // When
        final var config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getCredentialsProvider())
                .isEqualTo(new ProfileCredentialsProvider(profileName));
    }

    @Test
    public void shouldSupportStatic() {
        // Given
        final String accessKey = "AKIAIOSFODNN7EXAMPLE";
        final String secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
        setStaticCredentialsProvider(accessKey, secretKey);

        // When
        final PluginConfiguration config = getPluginConfiguration();

        // Then
        assertThat(config.getClient().getCredentialsProvider())
                .isEqualTo(new AWSStaticCredentialsProvider(accessKey, Secret.fromString(secretKey)));
    }
}
