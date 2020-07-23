package io.jenkins.plugins.credentials.secretsmanager.supplier;

import java.util.Optional;
import java.util.stream.Stream;

abstract class Optionals {
    private Optionals() {

    }

    /**
     * Polyfill for Java 9 Optional::stream.
     *
     * @param o the optional
     * @param <T> the type
     * @return the stream
     */
    static <T> Stream<T> stream(Optional<T> o) {
        return o.map(Stream::of).orElse(Stream.empty());
    }
}
