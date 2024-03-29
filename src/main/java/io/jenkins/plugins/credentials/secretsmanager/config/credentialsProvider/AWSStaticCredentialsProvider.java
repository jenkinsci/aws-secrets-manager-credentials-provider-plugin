package io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Objects;

public class AWSStaticCredentialsProvider extends CredentialsProvider {

    /**
     * The AWS access key (ID).
     */
    private String accessKey;

    /**
     * The AWS secret access key.
     */
    private Secret secretKey;

    @DataBoundConstructor
    public AWSStaticCredentialsProvider(String accessKey, Secret secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public AWSCredentialsProvider build() {
        final String secretKey = this.secretKey.getPlainText();
        final AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        return new com.amazonaws.auth.AWSStaticCredentialsProvider(creds);
    }

    public String getAccessKey() {
        return accessKey;
    }

    @DataBoundSetter
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Secret getSecretKey() {
        return secretKey;
    }

    @DataBoundSetter
    public void setSecretKey(Secret secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AWSStaticCredentialsProvider that = (AWSStaticCredentialsProvider) o;
        return Objects.equals(accessKey, that.accessKey) && Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKey, secretKey);
    }

    @Extension
    @Symbol("static")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends CredentialsProvider.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.statik();
        }

        public FormValidation doCheckAccessKey(@QueryParameter String accessKey) {
            if (Util.fixEmptyAndTrim(accessKey) == null) {
                return FormValidation.error("AWS Access Key must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSecretKey(@QueryParameter Secret secretKey) {
            if (Util.fixEmptyAndTrim(secretKey.getPlainText()) == null) {
                return FormValidation.error("AWS Secret Key must not be empty");
            }
            return FormValidation.ok();
        }
    }
}
