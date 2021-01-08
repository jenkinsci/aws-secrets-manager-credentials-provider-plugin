package io.jenkins.plugins.credentials.secretsmanager.config.transformer;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

public abstract class Transformer extends AbstractDescribableImpl<Transformer> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Transform the string using some operation.
     *
     * @param str the raw string
     * @return the transformed string
     */
    public abstract String transform(String str);

    /**
     * Apply the inverse transformation.
     *
     * @param str the string to transform back to what it would have been
     * @return the inverse-transformed string
     */
    public abstract String inverse(String str);

    public abstract static class DescriptorImpl extends Descriptor<Transformer> {
        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl(Class<? extends Transformer> clazz) {
            super(clazz);
        }
    }
}
