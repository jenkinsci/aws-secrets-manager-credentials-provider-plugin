package io.jenkins.plugins.credentials.secretsmanager.config.credentialsProvider;

import com.amazonaws.auth.AWSCredentialsProvider;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

/**
 * The AWSCredentialsProvider strategy configuration. (assume roles, instance profiles etc.)
 */
public abstract class CredentialsProvider extends AbstractDescribableImpl<CredentialsProvider> implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract AWSCredentialsProvider build();

    public abstract static class DescriptorImpl extends Descriptor<CredentialsProvider> {
        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl(Class<? extends CredentialsProvider> clazz) {
            super(clazz);
        }
    }
}