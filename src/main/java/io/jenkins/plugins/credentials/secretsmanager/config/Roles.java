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

public class Roles extends AbstractDescribableImpl<Roles> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ARN> arns;

    @DataBoundConstructor
    public Roles(List<ARN> arns) {
        this.arns = arns;
    }

    public List<ARN> getArns() {
        return arns;
    }

    @DataBoundSetter
    public void setArns(List<ARN> arns) {
        this.arns = arns;
    }

    @Extension
    @Symbol("roles")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<Roles> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.roles();
        }
    }
}
