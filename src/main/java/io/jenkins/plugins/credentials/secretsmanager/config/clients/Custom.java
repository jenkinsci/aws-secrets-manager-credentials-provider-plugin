package io.jenkins.plugins.credentials.secretsmanager.config.clients;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class Custom extends Clients {

    private List<Client> clients;

    @DataBoundConstructor
    public Custom(List<Client> clients) {
        this.clients = clients;
    }

    public List<Client> getClients() {
        return clients;
    }

    @DataBoundSetter
    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    public List<AWSSecretsManager> build() {
        return clients.stream()
                .map(Client::build)
                .collect(Collectors.toList());
    }

    @Extension
    @Symbol("custom")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Clients.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.customClients();
        }
    }
}
