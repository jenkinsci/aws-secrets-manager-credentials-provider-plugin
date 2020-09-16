package io.jenkins.plugins.credentials.secretsmanager.config.transformer.description;

import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Hide extends DescriptionTransformer {
    @DataBoundConstructor
    public Hide() {
        // no-op
    }

    @Override
    public String transform(String str) {
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Extension
    @Symbol("hide")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends DescriptionTransformer.DescriptorImpl {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.hide();
        }
    }
}
