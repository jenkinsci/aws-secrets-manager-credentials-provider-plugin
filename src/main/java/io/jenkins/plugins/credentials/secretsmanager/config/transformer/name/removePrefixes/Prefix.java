package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class Prefix extends AbstractDescribableImpl<Prefix> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;

    @DataBoundConstructor
    public Prefix(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @DataBoundSetter
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix value1 = (Prefix) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Extension
    @Symbol("prefix")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Prefix> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.prefix();
        }

        public FormValidation doCheckValue(@QueryParameter String value) {
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.warning("should not be empty");
            }
            
            return FormValidation.ok();
        }
    }
}
