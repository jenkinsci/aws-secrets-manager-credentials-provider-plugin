package io.jenkins.plugins.credentials.secretsmanager.config.credentials_provider;

import com.amazonaws.auth.AWSCredentialsProvider;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class DefaultAWSCredentialsProviderChain extends CredentialsProvider {

    @DataBoundConstructor
    public DefaultAWSCredentialsProviderChain() {

    }

    @Override
    public AWSCredentialsProvider build() {
        return new com.amazonaws.auth.DefaultAWSCredentialsProviderChain();
    }

    @Extension
    @Symbol("default")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.DefaultAWSCredentialsProviderChain();
        }
    }
}
