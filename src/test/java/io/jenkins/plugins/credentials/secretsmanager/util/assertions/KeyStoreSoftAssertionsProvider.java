package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import org.assertj.core.api.SoftAssertionsProvider;

import java.security.KeyStore;

public interface KeyStoreSoftAssertionsProvider extends SoftAssertionsProvider {
    default KeyStoreAssert assertThat(KeyStore actual) {
        return proxy(KeyStoreAssert.class, KeyStore.class, actual);
    }
}
