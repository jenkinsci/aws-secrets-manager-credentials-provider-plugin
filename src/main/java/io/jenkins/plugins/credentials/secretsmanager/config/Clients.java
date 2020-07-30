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
import java.util.List;

public class Clients extends AbstractDescribableImpl<Clients> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The secondary AWS Secrets Manager clients. */
    private List<Client> clients;

    @DataBoundConstructor
    public Clients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Client> getClients() {
        return clients;
    }

    @DataBoundSetter
    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @Extension
    @Symbol("clients")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Clients> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.clients();
        }
    }
}
