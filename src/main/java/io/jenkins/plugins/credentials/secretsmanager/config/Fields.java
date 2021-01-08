package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.Default;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.Transformer;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class Fields extends AbstractDescribableImpl<Fields> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether to show the secret's description or hide it. Defaults to true. */
    private Boolean description;

    /** How to present the secret's name. Defaults to passthrough (no transformation). */
    private Transformer name;

    @DataBoundConstructor
    public Fields(Boolean description, Transformer name) {
        this.description = description;
        this.name = name;
    }

    public Boolean getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(Boolean description) {
        this.description = description;
    }

    public Transformer getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(Transformer name) {
        this.name = name;
    }

    @Extension
    @Symbol("fields")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Fields> {

        public Transformer getDefaultName() {
            return new Default();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.fields();
        }
    }
}
