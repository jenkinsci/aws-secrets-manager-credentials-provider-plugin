package io.jenkins.plugins.credentials.secretsmanager.config.transformer.description;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

public abstract class DescriptionTransformer extends AbstractDescribableImpl<DescriptionTransformer> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Transform the string using some operation.
     *
     * @param str the raw string
     * @return the transformed string
     */
    public abstract String transform(String str);

    public abstract static class DescriptorImpl extends Descriptor<DescriptionTransformer> {

        protected DescriptorImpl() {
            super();
        }

        protected DescriptorImpl(Class<? extends DescriptionTransformer> clazz) {
            super(clazz);
        }
    }
}
