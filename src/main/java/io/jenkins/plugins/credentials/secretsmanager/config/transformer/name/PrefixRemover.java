package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.Util;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

@Restricted(NoExternalUse.class)
public class PrefixRemover {

    private final Set<String> prefixes;

    private PrefixRemover(Set<String> prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * Specify multiple possible prefixes that should be removed if seen in the string. Takes a set to ensure that the
     * prefixes are unique (this avoids duplication of effort when checking).
     */
    public static PrefixRemover removePrefixes(Set<String> prefixes) {
        return new PrefixRemover(prefixes);
    }

    public static PrefixRemover removePrefix(String prefix) {
        final Set<String> prefixes = Collections.singleton(prefix);
        return new PrefixRemover(prefixes);
    }

    public String from(String str) {
        if (prefixes == null) {
            return str;
        }

        for (String prefix: prefixes) {
            final String canonicalPrefix = fixNullAndTrim(prefix);

            if (str.startsWith(canonicalPrefix)) {
                return Pattern.compile(canonicalPrefix, Pattern.LITERAL)
                        .matcher(str)
                        .replaceFirst("");
            }
        }

        return str;
    }

    /**
     * Convert null to empty string, and trim whitespace.
     */
    private static String fixNullAndTrim(String s) {
        return Util.fixNull(s).trim();
    }
}
