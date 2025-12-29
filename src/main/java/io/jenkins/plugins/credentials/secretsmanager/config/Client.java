package io.jenkins.plugins.credentials.secretsmanager.config;

import org.apache.http.client.utils.URIBuilder;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class Client extends AbstractDescribableImpl<Client> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ClientConfiguration clientConfiguration;

    private CredentialsProvider credentialsProvider;

    private String endpointUrl;

    private String region;

    @DataBoundConstructor
    public Client(ClientConfiguration clientConfiguration, CredentialsProvider credentialsProvider, String endpointUrl, String region) {
        this.clientConfiguration = clientConfiguration;
        this.credentialsProvider = credentialsProvider;
        this.endpointUrl = endpointUrl;
        this.region = region;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    @DataBoundSetter
    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
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

    public String getEndpointUrl() {
        return endpointUrl;
    }

    @DataBoundSetter
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = Util.fixEmptyAndTrim(endpointUrl);
    }

    private static Optional<ProxyConfiguration> getProxyConfiguration() {
        // jenkins object could be null
        final var maybeJenkins = Optional.ofNullable(Jenkins.getInstanceOrNull());

        // proxy object could also be null
        return maybeJenkins.flatMap(j -> Optional.ofNullable(j.getProxy()));
    }

    static software.amazon.awssdk.http.apache.ProxyConfiguration toAwsProxyConfiguration(ProxyConfiguration conf) {
        final URI proxyEndpoint;
        try {
            proxyEndpoint = new URIBuilder()
                    .setHost(conf.getName())
                    .setPort(conf.getPort())
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        return software.amazon.awssdk.http.apache.ProxyConfiguration.builder()
                .nonProxyHosts(Collections.singleton(conf.getNoProxyHost()))
                .endpoint(proxyEndpoint)
                .username(conf.getUserName())
                .password(Secret.toString(conf.getSecretPassword()))
                .build();
    }

    public SecretsManagerClient build() {
        final var builder = SecretsManagerClient.builder();

        if (clientConfiguration != null) {
            builder.httpClient(clientConfiguration.build());
        } else {
            // If Jenkins has a system-wide proxy configuration set, use it.
            // Otherwise, leave the AWS client configuration as default.
            final var proxyConfiguration = getProxyConfiguration();

            proxyConfiguration.ifPresent(p -> {
                final var proxyClientConfiguration = toAwsProxyConfiguration(p);

                final var httpClient = ApacheHttpClient.builder()
                        .proxyConfiguration(proxyClientConfiguration)
                        .build();

                builder.httpClient(httpClient);
            });
        }

        if (credentialsProvider != null) {
            builder.credentialsProvider(credentialsProvider.build());
        }

        if (endpointUrl != null) {
            final var url = URI.create(endpointUrl);
            builder.endpointOverride(url);
        }

        if (region != null && !region.isEmpty()) {
            builder.region(Region.of(region));
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
                Objects.equals(endpointUrl, client.endpointUrl) &&
                Objects.equals(region, client.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientConfiguration, credentialsProvider, endpointUrl, region);
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
            for (final var region : Region.regions()) {
                final var metadata = region.metadata();
                regions.add(metadata.description(), metadata.id());
            }
            return regions;
        }
    }
}
