package io.jenkins.plugins.credentials.secretsmanager.config.credentials_provider;

import com.amazonaws.auth.AWSCredentialsProvider;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

public class ProfileCredentialsProvider extends CredentialsProvider {

    private String profileName;

    @DataBoundConstructor
    public ProfileCredentialsProvider(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    @DataBoundSetter
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @Override
    public AWSCredentialsProvider build() {
        return new com.amazonaws.auth.profile.ProfileCredentialsProvider(profileName);
    }

    @Extension
    @Symbol("profile")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.ProfileCredentialsProvider();
        }
    }
}
