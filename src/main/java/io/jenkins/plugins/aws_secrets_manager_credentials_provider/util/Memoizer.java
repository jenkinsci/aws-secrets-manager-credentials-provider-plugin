package io.jenkins.plugins.aws_secrets_manager_credentials_provider.util;

import com.google.common.base.Suppliers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Wrap the Guava memoizer utilities to make them Java 8 function friendly.
 */
public final class Memoizer {
    private Memoizer() {

    }

    /**
     * Memoize a supplier with expiration.
     *
     * @param <T> The return type of the supplier.
     * @param base The supplier to memoize.
     * @param duration The cache duration. Accurate to the nearest millisecond.
     * @return The supplier, memoized.
     */
    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Duration duration) {
        return Suppliers.memoizeWithExpiration(base::get, duration.toMillis(), TimeUnit.MILLISECONDS)::get;
    }
}
