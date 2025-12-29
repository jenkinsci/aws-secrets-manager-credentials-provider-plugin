package io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Objects;

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
    public AwsCredentialsProvider build() {
        return software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider.builder()
                .profileName(profileName)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileCredentialsProvider that = (ProfileCredentialsProvider) o;
        return Objects.equals(profileName, that.profileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileName);
    }

    @Extension
    @Symbol("profile")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.profile();
        }
    }
}