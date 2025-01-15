package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

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

    public com.amazonaws.ClientConfiguration build() {
        final var configuration = new com.amazonaws.ClientConfiguration();

        configuration.setNonProxyHosts(nonProxyHosts);
        configuration.setProxyHost(proxyHost);
        if (proxyPort != null) {
            configuration.setProxyPort(proxyPort);
        }
        configuration.setProxyUsername(proxyUsername);
        configuration.setProxyPassword(Secret.toString(proxyPassword));

        return configuration;
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
