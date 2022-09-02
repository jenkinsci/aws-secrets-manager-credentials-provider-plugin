package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import io.jenkins.plugins.credentials.secretsmanager.config.Value;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RemovePrefixes extends NameTransformer {

    private Set<Value> prefixes;

    @DataBoundConstructor
    public RemovePrefixes(Set<Value> prefixes) {
        this.prefixes = prefixes;
    }

    public Set<Value> getPrefixes() {
        return prefixes;
    }

    @DataBoundSetter
    public void setPrefixes(Set<Value> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public String transform(String str) {
        final Set<String> p = prefixes.stream()
                .map(Value::getValue)
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
