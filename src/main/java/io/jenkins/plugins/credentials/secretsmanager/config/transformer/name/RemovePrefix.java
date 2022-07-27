package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import io.jenkins.plugins.credentials.secretsmanager.Messages;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.regex.Pattern;

public class RemovePrefix extends NameTransformer {

    private String prefix;

    @DataBoundConstructor
    public RemovePrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @DataBoundSetter
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String transform(String str) {
        return theTransform(str, prefix);
    }

    private static String theTransform(String str, String prefix) {
        final String canonicalPrefix = fixNullAndTrim(prefix);

        if (str.startsWith(canonicalPrefix)) {
            return Pattern.compile(canonicalPrefix, Pattern.LITERAL)
                    .matcher(str)
                    .replaceFirst("");
        } else {
            return str;
        }
    }

    /**
     * Convert null to empty string, and trim whitespace.
     */
    private static String fixNullAndTrim(String s) {
        return Util.fixNull(s).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemovePrefix that = (RemovePrefix) o;
        return Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix);
    }

    @Extension
    @Symbol("removePrefix")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends NameTransformer.DescriptorImpl {
        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.removePrefix();
        }

        public FormValidation doCheckPrefix(@QueryParameter String prefix) {
            if (Util.fixEmptyAndTrim(prefix) == null) {
                return FormValidation.warning("Prefix should not be empty");
            }
            return FormValidation.ok();
        }
    }
}
