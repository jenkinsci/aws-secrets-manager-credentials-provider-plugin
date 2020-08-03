package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.clients.Clients;
import io.jenkins.plugins.credentials.secretsmanager.config.clients.DefaultClient;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Configuration for beta features.
 */
public class Beta extends AbstractDescribableImpl<Beta> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Clients clients;

    @DataBoundConstructor
    public Beta(Clients clients) {
        this.clients = clients;
    }

    public Clients getClients() {
        return clients;
    }

    @DataBoundSetter
    public void setClients(Clients clients) {
        this.clients = clients;
    }

    @Extension
    @Symbol("beta")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Beta> {

        public Clients getDefaultClients() {
            return new DefaultClient();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.beta();
        }
    }
}
