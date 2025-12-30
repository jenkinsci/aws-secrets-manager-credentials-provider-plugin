package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.apache.http.client.utils.URIBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;

/**
 * Configure the HTTP client used by the AWS SDK.
 */
public class ClientConfiguration extends AbstractDescribableImpl<ClientConfiguration> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nonProxyHosts;
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private Secret proxyPassword;

    @DataBoundConstructor
    public ClientConfiguration(String nonProxyHosts, String proxyHost, Integer proxyPort, String proxyUsername, Secret proxyPassword) {
        this.nonProxyHosts = nonProxyHosts;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    @DataBoundSetter
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    @DataBoundSetter
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    @DataBoundSetter
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    @DataBoundSetter
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public Secret getProxyPassword() {
        return proxyPassword;
    }

    @DataBoundSetter
    public void setProxyPassword(Secret proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConfiguration that = (ClientConfiguration) o;
        return Objects.equals(proxyPort, that.proxyPort) && Objects.equals(nonProxyHosts, that.nonProxyHosts) && Objects.equals(proxyHost, that.proxyHost) && Objects.equals(proxyUsername, that.proxyUsername) && Objects.equals(proxyPassword, that.proxyPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonProxyHosts, proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    public SdkHttpClient build() {
        return ApacheHttpClient.builder()
                .proxyConfiguration(buildProxyConfiguration())
                .build();
    }

    private ProxyConfiguration buildProxyConfiguration() {
        final var proxyEndpoint = buildProxyEndpoint();

        return ProxyConfiguration.builder()
                .nonProxyHosts(Collections.singleton(nonProxyHosts))
                .endpoint(proxyEndpoint)
                .username(proxyUsername)
                .password(Secret.toString(proxyPassword))
                .build();
    }

    private URI buildProxyEndpoint() {
        final var proxyEndpointBuilder = new URIBuilder()
                .setHost(proxyHost);

        if (proxyPort != null) {
            proxyEndpointBuilder.setPort(proxyPort);
        }

        try {
            return proxyEndpointBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Extension
    @Symbol("clientConfiguration")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<ClientConfiguration> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.clientConfiguration();
        }
    }
}
