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

@Deprecated
public class Tag extends AbstractDescribableImpl<Tag> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String value;

    @DataBoundConstructor
    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    @DataBoundSetter
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    @DataBoundSetter
    public void setValue(String value) {
        this.value = value;
    }

    @Extension
    @Symbol("tag")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Tag> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.tag();
        }
    }
}
