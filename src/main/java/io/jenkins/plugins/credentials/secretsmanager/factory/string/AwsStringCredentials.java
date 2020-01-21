package io.jenkins.plugins.credentials.secretsmanager.factory.string;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AwsStringCredentials extends BaseStandardCredentials implements StringCredentials {

    private final Supplier<Secret> value;

    public AwsStringCredentials(String id, String description, Supplier<Secret> value) {
        super(id, description);
        this.value = value;
    }

    @Nonnull
    @Override
    public Secret getSecret() {
        return value.get();
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.secretText();
        }
    }
}
