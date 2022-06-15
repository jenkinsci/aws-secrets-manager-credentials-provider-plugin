package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Default extends NameTransformer {

    @DataBoundConstructor
    public Default() {
        // no-op
    }

    @Override
    public String transform(String str) {
        return str;
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
    @Symbol("default")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends NameTransformer.DescriptorImpl {

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.deefault();
        }
    }
}
