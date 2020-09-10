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
import java.util.List;

/**
 * Config that will be applied to the secretsmanager:ListSecrets API call.
 */
public class ListSecrets extends AbstractDescribableImpl<ListSecrets> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Filters for the secretsmanager:ListSecrets API call.
     */
    private List<Filter> filters;

    @DataBoundConstructor
    public ListSecrets(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    @Extension
    @Symbol("listSecrets")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<ListSecrets> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.listSecrets();
        }
    }
}
