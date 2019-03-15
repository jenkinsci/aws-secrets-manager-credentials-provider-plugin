package io.jenkins.plugins.aws_secrets_manager_credentials_provider.config;

import io.jenkins.plugins.aws_secrets_manager_credentials_provider.Messages;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Filters extends AbstractDescribableImpl<Filters> implements Serializable {

    /**
     * Filter secrets received by their AWS tag. (To pass, a secret must have the specified tag key with one of the specified values.)
     */
    private Tag tag;

    @DataBoundConstructor
    public Filters(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Extension
    @Symbol("filters")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Filters> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.filters();
        }
    }
}
