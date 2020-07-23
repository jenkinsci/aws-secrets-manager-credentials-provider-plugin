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
 * Configuration for beta features.
 */
public class Beta extends AbstractDescribableImpl<Beta> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The IAM role ARNs to assume. For multi-account secrets retrieval.
     */
    private Roles roles;

    @DataBoundConstructor
    public Beta(Roles roles) {
        this.roles = roles;
    }

    public Roles getRoles() {
        return roles;
    }

    @DataBoundSetter
    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    @Extension
    @Symbol("beta")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Beta> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.beta();
        }
    }
}
