package io.jenkins.plugins.credentials.secretsmanager;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Port of Guava's ExpiringMemoizingSupplier which adds lazy duration lookup functionality.
 */
abstract class CustomSuppliers {

    private CustomSuppliers() {

    }

    /**
     * Returns a supplier that caches the instance supplied by the delegate and
     * removes the cached value after the specified time has passed. Subsequent
     * calls to {@code get()} return the cached value if the expiration time has
     * not passed. After the expiration time, a new value is retrieved, cached,
     * and returned. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     *
     * <p>The returned supplier is thread-safe. The supplier's serialized form
     * does not contain the cached value, which will be recalculated when {@code
     * get()} is called on the reserialized instance.
     *
     * @param duration the length of time after a value is created that it
     *     should stop being returned by subsequent {@code get()} calls
     * @throws IllegalArgumentException if {@code duration} is not positive
     * @since 2.0
     */
    public static <T> Supplier<T> memoizeWithExpiration(
            Supplier<T> delegate, Supplier<Duration> duration) {
        return new ExpiringMemoizingSupplier<>(delegate, duration);
    }

    static class ExpiringMemoizingSupplier<T>
            implements Supplier<T>, Serializable {
        final Supplier<T> delegate;
        final Supplier<Duration> duration;
        transient volatile T value;
        // The special value 0 means "not yet initialized".
        transient volatile long expirationNanos;

        ExpiringMemoizingSupplier(
                Supplier<T> delegate, Supplier<Duration> duration) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.duration = duration;
        }

        private long getDurationNanos() {
            Duration d = duration.get();
            Preconditions.checkArgument(!d.isNegative() && !d.isZero());
            return d.toNanos();
        }

        @Override
        public T get() {
            // Another variant of Double Checked Locking.
            //
            // We use two volatile reads.  We could reduce this to one by
            // putting our fields into a holder class, but (at least on x86)
            // the extra memory consumption and indirection are more
            // expensive than the extra volatile reads.
            long nanos = expirationNanos;
            long now = System.nanoTime();
            if (nanos == 0 || now - nanos >= 0) {
                synchronized (this) {
                    if (nanos == expirationNanos) {  // recheck for lost race
                        T t = delegate.get();
                        value = t;
                        nanos = now + getDurationNanos();
                        // In the very unlikely event that nanos is 0, set it to 1;
                        // no one will notice 1 ns of tardiness.
                        expirationNanos = (nanos == 0) ? 1 : nanos;
                        return t;
                    }
                }
            }
            return value;
        }

        private static final long serialVersionUID = 0;
    }
}
