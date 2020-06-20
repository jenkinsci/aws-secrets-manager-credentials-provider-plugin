package io.jenkins.plugins.credentials.secretsmanager.util.assertions;

import org.assertj.core.api.AbstractAssert;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class KeyStoreAssert extends AbstractAssert<KeyStoreAssert, KeyStore> {
    public KeyStoreAssert(KeyStore keyStore) {
        super(keyStore, KeyStoreAssert.class);
    }

    public KeyStoreAssert containsEntry(String alias, Certificate[] certificateChain) {
        isNotNull();

        final boolean foundEntry = keystoreToMap(actual)
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(alias) && Arrays.equals(entry.getValue(), certificateChain));

        if (!foundEntry) {
            failWithMessage("Expected KeyStore to contain entry alias=<%s> with certificate chain, but it did not", alias);
        }

        return this;
    }

    private static Map<String, Certificate[]> keystoreToMap(KeyStore keyStore) {
        final Map<String, Certificate[]> ks = new HashMap<>();

        try {
            final Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                final String a = aliases.nextElement();
                final Certificate[] certificateChain = keyStore.getCertificateChain(a);
                ks.put(a, certificateChain);
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }

        return ks;
    }
}
