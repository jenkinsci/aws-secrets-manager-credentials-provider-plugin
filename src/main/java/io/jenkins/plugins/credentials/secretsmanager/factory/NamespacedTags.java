package io.jenkins.plugins.credentials.secretsmanager.factory;

import java.util.Map;
import java.util.Optional;

/**
 * Provides access to namespaced tags on the Secrets Manager entry.
 */
class NamespacedTags implements Tags {
    private static final String NAMESPACE = "jenkins:credentials:";

    private final Map<String, String> tags;

    NamespacedTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public Optional<String> get(String key) {
        final var namespacedKey = namespaced(key);
        final var value = tags.get(namespacedKey);
        return Optional.ofNullable(value);
    }

    private static String namespaced(String key) {
        return NAMESPACE + key;
    }
}
