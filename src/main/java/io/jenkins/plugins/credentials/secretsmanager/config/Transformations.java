package io.jenkins.plugins.credentials.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.description.DescriptionTransformer;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.NameTransformer;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class Transformations extends AbstractDescribableImpl<Transformations> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Whether to show the secret's description or hide it. Defaults to true. */
    private DescriptionTransformer description;

    /** How to present the secret's name. Defaults to passthrough (no transformation). */
    private NameTransformer name;

    @DataBoundConstructor
    public Transformations(DescriptionTransformer description, NameTransformer name) {
        this.description = description;
        this.name = name;
    }

    public DescriptionTransformer getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(DescriptionTransformer description) {
        this.description = description;
    }

    public NameTransformer getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(NameTransformer name) {
        this.name = name;
    }

    @Extension
    @Symbol("transformations")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Transformations> {

        public NameTransformer getDefaultName() {
            return new io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.Default();
        }

        public DescriptionTransformer getDefaultDescription() {
            return new io.jenkins.plugins.credentials.secretsmanager.config.transformer.description.Default();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.transformations();
        }
    }
}
