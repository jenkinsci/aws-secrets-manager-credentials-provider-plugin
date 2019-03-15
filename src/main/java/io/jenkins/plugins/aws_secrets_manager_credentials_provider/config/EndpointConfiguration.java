package io.jenkins.plugins.aws_secrets_manager_credentials_provider.config;

import io.jenkins.plugins.aws_secrets_manager_credentials_provider.Messages;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class EndpointConfiguration extends AbstractDescribableImpl<EndpointConfiguration> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serviceEndpoint;
    private String signingRegion;

    @DataBoundConstructor
    public EndpointConfiguration(String serviceEndpoint, String signingRegion) {
        this.serviceEndpoint = serviceEndpoint;
        this.signingRegion = signingRegion;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public String getSigningRegion() {
        return signingRegion;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setSigningRegion(String signingRegion) {
        this.signingRegion = signingRegion;
    }

    @Override
    public String toString() {
        return "Service Endpoint = " + serviceEndpoint + ", Signing Region = " + signingRegion;
    }

    @Extension
    @Symbol("endpointConfiguration")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<EndpointConfiguration> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.endpointConfiguration();
        }
    }
}
