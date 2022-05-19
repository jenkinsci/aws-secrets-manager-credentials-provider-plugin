package io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider;

import com.amazonaws.auth.AWSCredentialsProvider;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Objects;

public class STSAssumeRoleSessionCredentialsProvider extends CredentialsProvider {

    private static final int DEFAULT_ROLE_SESSION_DURATION_SECONDS = 900;

    private String roleArn;

    private String roleSessionName;

    @DataBoundConstructor
    public STSAssumeRoleSessionCredentialsProvider(String roleArn, String roleSessionName) {
        this.roleArn = roleArn;
        this.roleSessionName = roleSessionName;
    }

    public String getRoleArn() {
        return roleArn;
    }

    @DataBoundSetter
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public String getRoleSessionName() {
        return roleSessionName;
    }

    @DataBoundSetter
    public void setRoleSessionName(String roleSessionName) {
        this.roleSessionName = roleSessionName;
    }

    @Override
    public AWSCredentialsProvider build() {
        return new com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, roleSessionName)
                .withRoleSessionDurationSeconds(DEFAULT_ROLE_SESSION_DURATION_SECONDS)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        STSAssumeRoleSessionCredentialsProvider that = (STSAssumeRoleSessionCredentialsProvider) o;
        return Objects.equals(roleArn, that.roleArn) &&
                Objects.equals(roleSessionName, that.roleSessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleArn, roleSessionName);
    }

    @Extension
    @Symbol("assumeRole")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.assumeRole();
        }
    }
}