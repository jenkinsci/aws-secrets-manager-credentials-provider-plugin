package io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider;

import com.amazonaws.auth.AWSCredentialsProvider;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DefaultAWSCredentialsProviderChain extends CredentialsProvider {

    @DataBoundConstructor
    public DefaultAWSCredentialsProviderChain() {

    }

    @Override
    public AWSCredentialsProvider build() {
        return new com.amazonaws.auth.DefaultAWSCredentialsProviderChain();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Extension
    @Symbol("default")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.defaultClient();
        }
    }
}