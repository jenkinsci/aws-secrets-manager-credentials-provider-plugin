package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

public abstract class NameTransformer extends AbstractDescribableImpl<NameTransformer> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Transform the string using some operation.
     *
     * @param str the raw string
     * @return the transformed string
     */
    public abstract String transform(String str);

    public abstract static class DescriptorImpl extends Descriptor<NameTransformer> {
        /**
         * See {@link Descriptor#Descriptor()}
         */
        protected DescriptorImpl() {
            super();
        }

        /**
         * See {@link Descriptor#Descriptor(Class)}
         */
        protected DescriptorImpl(Class<? extends NameTransformer> clazz) {
            super(clazz);
        }
    }
}
