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

public class Name extends AbstractDescribableImpl<Name> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String pattern;

    @DataBoundConstructor
    public Name(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    @DataBoundSetter
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Extension
    @Symbol("filters")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Name> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.name();
        }
    }
}
