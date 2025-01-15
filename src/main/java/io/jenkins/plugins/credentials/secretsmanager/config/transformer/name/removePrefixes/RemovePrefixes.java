package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.removePrefixes;

import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.NameTransformer;
import io.jenkins.plugins.credentials.secretsmanager.config.transformer.name.PrefixRemover;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RemovePrefixes extends NameTransformer {

    private Set<Prefix> prefixes;

    @DataBoundConstructor
    public RemovePrefixes(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public Set<Prefix> getPrefixes() {
        return prefixes;
    }

    @DataBoundSetter
    public void setPrefixes(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public String transform(String str) {
        final Set<String> p = prefixes.stream()
                .map(Prefix::getValue)
                .collect(Collectors.toSet());

        return PrefixRemover.removePrefixes(p).from(str);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemovePrefixes that = (RemovePrefixes) o;
        return Objects.equals(prefixes, that.prefixes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefixes);
    }

    @Extension
    @Symbol("removePrefixes")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends NameTransformer.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.removePrefixes();
        }
    }
}
