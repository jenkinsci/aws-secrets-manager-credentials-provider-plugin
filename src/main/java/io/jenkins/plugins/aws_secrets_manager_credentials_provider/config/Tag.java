package io.jenkins.plugins.aws_secrets_manager_credentials_provider.config;

import com.google.common.base.Objects;

import io.jenkins.plugins.aws_secrets_manager_credentials_provider.Messages;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equal(key, tag.key) &&
                Objects.equal(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, value);
    }

    @Extension
    @Symbol("filters")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Tag> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.tag();
        }
    }
}
