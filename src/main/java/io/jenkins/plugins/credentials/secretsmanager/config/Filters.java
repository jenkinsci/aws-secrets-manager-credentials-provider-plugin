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

/**
 * This was deprecated when AWS introduced server-side filters in the secretsmanager:ListSecrets API call.
 */
@Deprecated
public class Filters extends AbstractDescribableImpl<Filters> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Tag tag;

    @DataBoundConstructor
    public Filters(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @DataBoundSetter
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Deprecated
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
