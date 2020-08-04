package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.CredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class Client extends AbstractDescribableImpl<Client> implements Serializable {

    private static final long serialVersionUID = 1L;

    private CredentialsProvider credentialsProvider;

    private EndpointConfiguration endpointConfiguration;

    private Region region;

    @DataBoundConstructor
    public Client(CredentialsProvider credentialsProvider, EndpointConfiguration endpointConfiguration, Region region) {
        this.credentialsProvider = credentialsProvider;
        this.endpointConfiguration = endpointConfiguration;
        this.region = region;
    }

    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @DataBoundSetter
    public void setEndpointConfiguration(EndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    @DataBoundSetter
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public Region getRegion() {
        return region;
    }

    @DataBoundSetter
    public void setRegion(Region region) {
        this.region = region;
    }

    public AWSSecretsManager build() {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();

        if (credentialsProvider != null) {
            builder.setCredentials(credentialsProvider.build());
        }

        if (endpointConfiguration != null) {
            builder.setEndpointConfiguration(endpointConfiguration.build());
        }

        if (region != null) {
            builder.setRegion(region.getRegion());
        }

        return builder.build();
    }

    @Extension
    @Symbol("client")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Client> {

        public CredentialsProvider getDefaultCredentialsProvider() {
            return new DefaultAWSCredentialsProviderChain();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.client();
        }
    }
}
