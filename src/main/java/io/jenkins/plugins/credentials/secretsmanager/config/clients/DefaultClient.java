package io.jenkins.plugins.credentials.secretsmanager.config.clients;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class DefaultClient extends Clients {

    @DataBoundConstructor
    public DefaultClient() {

    }

    @Override
    public List<AWSSecretsManager> build() {
        final AWSSecretsManager secretsManager = AWSSecretsManagerClientBuilder.defaultClient();
        return Collections.singletonList(secretsManager);
    }

    @Extension
    @Symbol("default")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Clients.DescriptorImpl {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.defaultClient();
        }
    }
}
