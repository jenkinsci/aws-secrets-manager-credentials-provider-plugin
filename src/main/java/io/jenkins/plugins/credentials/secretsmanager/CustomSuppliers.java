package io.jenkins.plugins.credentials.secretsmanager;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Memoizing supplier with expiry and lazy duration lookup, derived from Guava's ExpiringMemoizingSupplier.
 *
 * <p>Unlike Guava's, the delegate (which here does network I/O) never runs while holding a monitor that other
 * callers need:
 * <ul>
 *   <li>Only one caller refreshes at a time. If a value is already cached, other callers get it (stale) immediately
 *   instead of queueing behind the refresh.</li>
 *   <li>If there is no value yet, callers share the single in-flight load and its outcome.</li>
 *   <li>If a refresh fails, the previous value (if any) keeps being served, and the failure is remembered for a
 *   short backoff period so that callers do not each repeat a failing (possibly slow) refresh.</li>
 *   <li>Expiry is measured from when the refresh completes, not from when the caller arrived.</li>
 * </ul>
 *
 * @see <a href="https://github.com/google/guava/blob/v29.0/guava/src/com/google/common/base/Suppliers.java">Suppliers.java</a>
 */
final class CustomSuppliers {

    private static final Duration DEFAULT_FAILURE_BACKOFF = Duration.ofSeconds(5);

    private CustomSuppliers() {

    }

    public static <T> Supplier<T> memoizeWithExpiration(
            Supplier<T> delegate, Supplier<Duration> duration) {
        return memoizeWithExpiration(delegate, duration, DEFAULT_FAILURE_BACKOFF);
    }

    static <T> Supplier<T> memoizeWithExpiration(
            Supplier<T> delegate, Supplier<Duration> duration, Duration failureBackoff) {
        return new ExpiringMemoizingSupplier<>(delegate, duration, failureBackoff);
    }

    static class ExpiringMemoizingSupplier<T> implements Supplier<T> {

        private static final Logger LOG = Logger.getLogger(ExpiringMemoizingSupplier.class.getName());

        private final Supplier<T> delegate;
        private final Supplier<Duration> duration;
        private final long failureBackoffNanos;

        // All mutable state below is guarded by 'this'. The monitor is only ever held for field access, never
        // across a call to the delegate.
        private boolean loaded;
        private T value;
        private long expirationNanos;
        private RuntimeException lastFailure;
        private long retryAfterNanos;
        private CompletableFuture<T> inFlight;

        ExpiringMemoizingSupplier(
                Supplier<T> delegate, Supplier<Duration> duration, Duration failureBackoff) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.duration = duration;
            this.failureBackoffNanos = failureBackoff.toNanos();
        }

        private long getDurationNanos() {
            Duration d = duration.get();
            Preconditions.checkArgument(!d.isNegative() && !d.isZero());
            return d.toNanos();
        }

        @Override
        public T get() {
            final CompletableFuture<T> load;
            final boolean refresher;

            synchronized (this) {
                final long now = System.nanoTime();

                if (loaded && now - expirationNanos < 0) {
                    return value;
                }

                if (lastFailure != null && now - retryAfterNanos < 0) {
                    // A refresh failed recently; don't pile on another one yet
                    if (loaded) {
                        return value;
                    }
                    throw lastFailure;
                }

                if (inFlight != null) {
                    if (loaded) {
                        // Somebody else is refreshing; make do with the old value
                        return value;
                    }
                    // Nothing to serve yet; wait for the in-flight load
                    load = inFlight;
                    refresher = false;
                } else {
                    // This caller becomes the refresher
                    load = inFlight = new CompletableFuture<>();
                    refresher = true;
                }
            }

            return refresher ? refresh(load) : await(load);
        }

        /**
         * Run the delegate. Must be called by the caller that installed {@code load} as {@link #inFlight}, while
         * NOT holding the monitor.
         */
        private T refresh(CompletableFuture<T> load) {
            final T fresh;
            final long durationNanos;
            try {
                fresh = delegate.get();
                durationNanos = getDurationNanos();
            } catch (RuntimeException e) {
                synchronized (this) {
                    lastFailure = e;
                    retryAfterNanos = System.nanoTime() + failureBackoffNanos;
                    inFlight = null;
                    load.completeExceptionally(e);
                    if (loaded) {
                        LOG.log(Level.WARNING, "Refresh failed, serving stale value: message=[{0}]", e.getMessage());
                        return value;
                    }
                }
                throw e;
            } catch (Throwable t) {
                // Don't leave waiters hanging, but don't cache Errors
                synchronized (this) {
                    inFlight = null;
                    load.completeExceptionally(t);
                }
                throw t;
            }

            synchronized (this) {
                value = fresh;
                loaded = true;
                // Measure from now rather than from when the refresh began
                expirationNanos = System.nanoTime() + durationNanos;
                lastFailure = null;
                inFlight = null;
                load.complete(fresh);
            }
            return fresh;
        }

        private T await(CompletableFuture<T> load) {
            try {
                return load.join();
            } catch (CompletionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw e;
            }
        }
    }
}
