package io.jenkins.plugins.credentials.secretsmanager;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Adaptation of Guava's SuppliersTest.
 *
 * @see <a href="https://github.com/google/guava/blob/v29.0/guava-tests/test/com/google/common/base/SuppliersTest.java">SuppliersTest.java</a>
 */
public class CustomSuppliersTest {

    @Test
    public void shouldMemoizeValues() throws InterruptedException {
        CountingSupplier countingSupplier = new CountingSupplier();

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(countingSupplier, () -> Duration.ofMillis(75));

        checkExpiration(countingSupplier, memoizedSupplier);
    }

    @Test
    public void shouldNotMemoizeExceptions() {
        ThrowingSupplier throwingSupplier = new ThrowingSupplier();

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(throwingSupplier, () -> Duration.ofSeconds(1));

        assertThatNullPointerException()
                .isThrownBy(memoizedSupplier::get);

        assertThatNullPointerException()
                .isThrownBy(memoizedSupplier::get);

        // Exception should not have been cached
        assertThat(throwingSupplier.calls).isEqualTo(2);
    }

    @Test
    public void shouldRejectNegativeDuration() {
        CountingSupplier countingSupplier = new CountingSupplier();

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(countingSupplier, () -> Duration.ofMillis(-1));

        assertThatIllegalArgumentException()
                .isThrownBy(memoizedSupplier::get);
    }

    @Test
    public void shouldRejectZeroDuration() {
        CountingSupplier countingSupplier = new CountingSupplier();

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(countingSupplier, () -> Duration.ZERO);

        assertThatIllegalArgumentException()
                .isThrownBy(memoizedSupplier::get);
    }

    @Test
    public void shouldBeThreadSafe() throws Throwable {
        Function<Supplier<Boolean>, Supplier<Boolean>> memoizer =
                supplier -> CustomSuppliers.memoizeWithExpiration(supplier, () -> Duration.ofNanos(Long.MAX_VALUE));

        final AtomicInteger count = new AtomicInteger(0);
        final AtomicReference<Throwable> thrown = new AtomicReference<>(null);
        final int numThreads = 3;
        final Thread[] threads = new Thread[numThreads];
        final long timeout = TimeUnit.SECONDS.toNanos(60);

        final Supplier<Boolean> supplier =
                new Supplier<Boolean>() {
                    boolean isWaiting(Thread thread) {
                        switch (thread.getState()) {
                            case BLOCKED:
                            case WAITING:
                            case TIMED_WAITING:
                                return true;
                            default:
                                return false;
                        }
                    }

                    int waitingThreads() {
                        int waitingThreads = 0;
                        for (Thread thread : threads) {
                            if (isWaiting(thread)) {
                                waitingThreads++;
                            }
                        }
                        return waitingThreads;
                    }

                    @Override
                    public Boolean get() {
                        // Check that this method is called exactly once, by the first
                        // thread to synchronize.
                        long t0 = System.nanoTime();
                        while (waitingThreads() != numThreads - 1) {
                            if (System.nanoTime() - t0 > timeout) {
                                thrown.set(
                                        new TimeoutException(
                                                "timed out waiting for other threads to block"
                                                        + " synchronizing on supplier"));
                                break;
                            }
                            Thread.yield();
                        }
                        count.getAndIncrement();
                        return Boolean.TRUE;
                    }
                };

        final Supplier<Boolean> memoizedSupplier = memoizer.apply(supplier);

        for (int i = 0; i < numThreads; i++) {
            threads[i] =
                    new Thread(() -> assertThat(memoizedSupplier.get()).isEqualTo(Boolean.TRUE));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        if (thrown.get() != null) {
            throw thrown.get();
        }

        assertThat(count.get()).isEqualTo(1);
    }

    private static void checkExpiration(
            CountingSupplier countingSupplier, Supplier<Integer> memoizedSupplier)
            throws InterruptedException {
        // the underlying supplier hasn't executed yet
        assertThat(countingSupplier.calls).isEqualTo(0);

        assertThat(memoizedSupplier.get()).isEqualTo(10);
        // now it has
        assertThat(countingSupplier.calls).isEqualTo(1);

        assertThat(memoizedSupplier.get()).isEqualTo(10);
        // it still should only have executed once due to memoization
        assertThat(countingSupplier.calls).isEqualTo(1);

        Thread.sleep(150);

        assertThat(memoizedSupplier.get()).isEqualTo(20);
        // old value expired
        assertThat(countingSupplier.calls).isEqualTo(2);

        assertThat(memoizedSupplier.get()).isEqualTo(20);
        // it still should only have executed twice due to memoization
        assertThat(countingSupplier.calls).isEqualTo(2);
    }

    private static class CountingSupplier implements Supplier<Integer> {
        int calls = 0;

        @Override
        public Integer get() {
            calls++;
            return calls * 10;
        }
    }

    private static class ThrowingSupplier implements Supplier<Integer> {
        int calls = 0;

        @Override
        public Integer get() {
            calls++;
            throw new NullPointerException();
        }
    }
}