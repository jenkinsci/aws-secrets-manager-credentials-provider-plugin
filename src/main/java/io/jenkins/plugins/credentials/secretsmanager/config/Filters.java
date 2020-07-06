package io.jenkins.plugins.credentials.secretsmanager.config;

import io.jenkins.plugins.credentials.secretsmanager.Messages;
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
     * Filter secrets received by their AWS tag. (To pass, a secret must have the specified tag key
     * with one of the specified values.)
     */
    private Tag tag;
    private Name name;

    @DataBoundConstructor
    public Filters(Tag tag, Name name) {
        this.tag = tag;
        this.name = name;
    }

    public Tag getTag() {
        return tag;
    }

    public Name getName() {
        return name;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setName(Name name) {
        this.name = name;
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
