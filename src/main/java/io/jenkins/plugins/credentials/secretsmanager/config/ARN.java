package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class ARN extends AbstractDescribableImpl<ARN> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;

    @DataBoundConstructor
    public ARN(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @DataBoundSetter
    public void setValue(String value) {
        this.value = value;
    }

    @Extension
    @Symbol("arn")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<ARN> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.arn();
        }

    }
}
