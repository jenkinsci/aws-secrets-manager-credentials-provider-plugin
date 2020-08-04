package io.jenkins.plugins.credentials.secretsmanager.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Clients extends AbstractDescribableImpl<Clients> implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public List<AWSSecretsManager> build() {
        if (clients == null) {
            final AWSSecretsManager secretsManager = AWSSecretsManagerClientBuilder.defaultClient();
            return Collections.singletonList(secretsManager);
        }

        return clients.stream()
                .map(Client::build)
                .collect(Collectors.toList());
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
