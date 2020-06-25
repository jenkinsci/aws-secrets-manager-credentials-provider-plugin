package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class Value extends AbstractDescribableImpl<Value> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;

    @DataBoundConstructor
    public Value(String value) {
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
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Extension
    @Symbol("value")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Value> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.value();
        }

    }
}
