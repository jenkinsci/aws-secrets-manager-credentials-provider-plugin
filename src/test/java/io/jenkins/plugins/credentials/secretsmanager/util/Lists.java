package io.jenkins.plugins.credentials.secretsmanager.util;

import java.util.*;

/**
 * Polyfill for the Java 9 List.of API methods.
 */
public abstract class Lists {

    private Lists() {

    }

    @SafeVarargs
    public static <V> List<V> of(V... elems) {
        return Arrays.asList(elems);
    }
}
