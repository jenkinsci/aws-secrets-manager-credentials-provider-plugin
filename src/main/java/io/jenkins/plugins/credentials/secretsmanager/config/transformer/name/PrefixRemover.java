package io.jenkins.plugins.credentials.secretsmanager.config.transformer.name;

import hudson.Util;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Collections;
import java.util.Set;

/**
 * A non-regex based remover of string prefixes
 */
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

        final int longestMatchingPrefix = longestMatchingPrefix(str, prefixes);

        return str.substring(longestMatchingPrefix);
    }

    /**
     * return the length of the longest matching prefix, or zero if the string did not contain any of the prefixes
     */
    private static int longestMatchingPrefix(String str, Set<String> prefixes) {
        int longestMatch = 0;

        for (String prefix: prefixes) {
            final String canonicalPrefix = fixNullAndTrim(prefix);

            if (str.startsWith(canonicalPrefix)) {
                final int prefixLength = canonicalPrefix.length();
                if (prefixLength > longestMatch) {
                    longestMatch = prefixLength;
                }
            }
        }

        return longestMatch;
    }

    /**
     * Convert null to empty string, and trim whitespace.
     */
    private static String fixNullAndTrim(String s) {
        return Util.fixNull(s).trim();
    }
}
