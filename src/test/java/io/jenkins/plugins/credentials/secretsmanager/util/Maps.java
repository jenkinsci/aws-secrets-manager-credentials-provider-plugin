package io.jenkins.plugins.credentials.secretsmanager.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Polyfill for the Java 9 Map.of API methods.
 */
public final class Maps {

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2) {
        final Map<K, V> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return Collections.unmodifiableMap(m);
    }

    public static <K,V> Map<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final Map<K, V> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        m.put(k4, v4);
        return Collections.unmodifiableMap(m);
    }
}
