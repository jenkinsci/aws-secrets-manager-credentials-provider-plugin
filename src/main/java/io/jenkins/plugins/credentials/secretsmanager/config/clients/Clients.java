package io.jenkins.plugins.credentials.secretsmanager.config.clients;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;
import java.util.List;

public abstract class Clients extends AbstractDescribableImpl<Clients> implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract List<AWSSecretsManager> build();

    public abstract static class DescriptorImpl extends Descriptor<Clients> {
        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        protected DescriptorImpl(Class<? extends Clients> clazz) {
            super(clazz);
        }
    }
}
