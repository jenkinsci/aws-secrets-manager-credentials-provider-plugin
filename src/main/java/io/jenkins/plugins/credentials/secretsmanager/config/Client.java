package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.CredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class Client extends AbstractDescribableImpl<Client> implements Serializable {

    private static final long serialVersionUID = 1L;

    private CredentialsProvider credentialsProvider;

    private EndpointConfiguration endpointConfiguration;

    private String region;

    @DataBoundConstructor
    public Client(CredentialsProvider credentialsProvider, EndpointConfiguration endpointConfiguration, String region) {
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

    public String getRegion() {
        return region;
    }

    @DataBoundSetter
    public void setRegion(String region) {
        this.region = Util.fixEmptyAndTrim(region);
    }

    public AWSSecretsManager build() {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();

        if (credentialsProvider != null) {
            builder.setCredentials(credentialsProvider.build());
        }

        if (endpointConfiguration != null) {
            builder.setEndpointConfiguration(endpointConfiguration.build());
        }

        if (region != null && !region.isEmpty()) {
            builder.setRegion(region);
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(credentialsProvider, client.credentialsProvider) &&
                Objects.equals(endpointConfiguration, client.endpointConfiguration) &&
                Objects.equals(region, client.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialsProvider, endpointConfiguration, region);
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

        public ListBoxModel doFillRegionItems() {
            final ListBoxModel regions = new ListBoxModel();
            regions.add("", "");
            for (Regions s : Regions.values()) {
                regions.add(s.getDescription(), s.getName());
            }
            return regions;
        }
    }
}
