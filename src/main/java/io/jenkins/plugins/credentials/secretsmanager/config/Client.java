package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class Client extends AbstractDescribableImpl<Client> implements Serializable {

    private static final long serialVersionUID = 1L;

    private EndpointConfiguration endpointConfiguration;

    /** The custom IAM role ARN. **/
    private String role;

    @DataBoundConstructor
    public Client(EndpointConfiguration endpointConfiguration, String role) {
        this.endpointConfiguration = endpointConfiguration;
        this.role = role;
    }

    public EndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @DataBoundSetter
    public void setEndpointConfiguration(EndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    public String getRole() {
        return role;
    }

    @DataBoundSetter
    public void setRole(String role) {
        this.role = role;
    }

    @Extension
    @Symbol("client")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Client> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.client();
        }
    }
}
