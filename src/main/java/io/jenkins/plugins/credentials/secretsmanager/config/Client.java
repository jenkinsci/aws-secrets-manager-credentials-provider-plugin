package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.CredentialsProvider;
import io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider.DefaultAWSCredentialsProviderChain;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class Client extends AbstractDescribableImpl<Client> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ClientConfiguration clientConfiguration;

    private CredentialsProvider credentialsProvider;

    private EndpointConfiguration endpointConfiguration;

    private String region;

    @DataBoundConstructor
    public Client(ClientConfiguration clientConfiguration, CredentialsProvider credentialsProvider, EndpointConfiguration endpointConfiguration, String region) {
        this.clientConfiguration = clientConfiguration;
        this.credentialsProvider = credentialsProvider;
        this.endpointConfiguration = endpointConfiguration;
        this.region = region;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    @DataBoundSetter
    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
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

    private static Optional<ProxyConfiguration> getProxyConfiguration() {
        // jenkins object could be null
        final var maybeJenkins = Optional.ofNullable(Jenkins.getInstanceOrNull());

        // proxy object could also be null
        return maybeJenkins.flatMap(j -> Optional.ofNullable(j.getProxy()));
    }

    static com.amazonaws.ClientConfiguration toClientConfiguration(ProxyConfiguration proxyConfiguration) {
        final var configuration = new com.amazonaws.ClientConfiguration();

        configuration.setProxyHost(proxyConfiguration.getName());
        configuration.setProxyPort(proxyConfiguration.getPort());
        configuration.setProxyUsername(proxyConfiguration.getUserName());
        configuration.setProxyPassword(Secret.toString(proxyConfiguration.getSecretPassword()));
        configuration.setNonProxyHosts(proxyConfiguration.getNoProxyHost());

        return configuration;
    }

    public AWSSecretsManager build() {
        final AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();

        if (clientConfiguration != null) {
            builder.setClientConfiguration(clientConfiguration.build());
        } else {
            // If Jenkins has a system-wide proxy configuration set, use it.
            // Otherwise, leave the AWS client configuration as default.
            final var proxyConfiguration = getProxyConfiguration();

            proxyConfiguration.ifPresent(p -> {
                final var proxyClientConfiguration = toClientConfiguration(p);

                builder.setClientConfiguration(proxyClientConfiguration);
            });
        }

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
        return Objects.equals(clientConfiguration, client.clientConfiguration) &&
                Objects.equals(credentialsProvider, client.credentialsProvider) &&
                Objects.equals(endpointConfiguration, client.endpointConfiguration) &&
                Objects.equals(region, client.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientConfiguration, credentialsProvider, endpointConfiguration, region);
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
