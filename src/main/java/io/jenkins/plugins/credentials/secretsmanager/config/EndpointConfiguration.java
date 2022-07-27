package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class EndpointConfiguration extends AbstractDescribableImpl<EndpointConfiguration>
        implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointConfiguration that = (EndpointConfiguration) o;
        return Objects.equals(serviceEndpoint, that.serviceEndpoint) &&
                Objects.equals(signingRegion, that.signingRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceEndpoint, signingRegion);
    }

    public AwsClientBuilder.EndpointConfiguration build() {
        if (serviceEndpoint == null || signingRegion == null) {
            return null;
        }

        return new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion);
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

        public ListBoxModel doFillSigningRegionItems() {
            final ListBoxModel regions = new ListBoxModel();
            regions.add("", "");
            for (Regions s : Regions.values()) {
                regions.add(s.getDescription(), s.getName());
            }
            return regions;
        }

    }
}
