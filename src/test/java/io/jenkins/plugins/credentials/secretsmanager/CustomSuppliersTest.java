package io.jenkins.plugins.credentials.secretsmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    public void shouldRetryAfterFailureBackoff() throws InterruptedException {
        ThrowingSupplier throwingSupplier = new ThrowingSupplier();

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(throwingSupplier, () -> Duration.ofSeconds(1), Duration.ofMillis(100));

        assertThatNullPointerException()
                .isThrownBy(memoizedSupplier::get);

        // Failure is remembered during the backoff, so the delegate isn't hit again
        assertThatNullPointerException()
                .isThrownBy(memoizedSupplier::get);
        assertThat(throwingSupplier.calls).isEqualTo(1);

        Thread.sleep(200);

        assertThatNullPointerException()
                .isThrownBy(memoizedSupplier::get);
        assertThat(throwingSupplier.calls).isEqualTo(2);
    }

    @Test
    public void shouldServeStaleValueWhenRefreshFails() throws InterruptedException {
        final AtomicInteger calls = new AtomicInteger();
        final Supplier<Integer> flaky = () -> {
            if (calls.incrementAndGet() == 1) {
                return 42;
            }
            throw new IllegalStateException("AWS is down");
        };

        Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(flaky, () -> Duration.ofMillis(50), Duration.ofSeconds(60));

        assertThat(memoizedSupplier.get()).isEqualTo(42);

        Thread.sleep(100);

        // Refresh fails, old value survives
        assertThat(memoizedSupplier.get()).isEqualTo(42);
        // Within the backoff, no further refresh attempts
        assertThat(memoizedSupplier.get()).isEqualTo(42);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void shouldShareOneColdLoadFailureBetweenWaiters() throws Throwable {
        final int numThreads = 3;
        final AtomicInteger calls = new AtomicInteger();
        final CountDownLatch release = new CountDownLatch(1);

        final Supplier<Integer> slowFailure = () -> {
            calls.incrementAndGet();
            await(release);
            throw new IllegalStateException("timeout");
        };

        final Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(slowFailure, () -> Duration.ofSeconds(60));

        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            final List<Future<Integer>> futures = new ArrayList<>();
            final List<Thread> callers = new CopyOnWriteArrayList<>();
            for (int i = 0; i < numThreads; i++) {
                futures.add(executor.submit(() -> {
                    callers.add(Thread.currentThread());
                    return memoizedSupplier.get();
                }));
            }

            // The refresher is parked inside the delegate; the others are parked on its result
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (callers.size() < numThreads || callers.stream().anyMatch(t -> t.getState() != Thread.State.WAITING)) {
                assertThat(System.nanoTime()).isLessThan(deadline);
                Thread.sleep(5);
            }
            release.countDown();

            for (Future<Integer> future : futures) {
                assertThatThrownBy(() -> future.get(30, TimeUnit.SECONDS))
                        .hasRootCauseInstanceOf(IllegalStateException.class);
            }
            assertThat(calls.get()).isEqualTo(1);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void shouldNotBlockCallersDuringRefresh() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        final CountDownLatch refreshStarted = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        final Supplier<Integer> slowOnRefresh = () -> {
            final int call = calls.incrementAndGet();
            if (call > 1) {
                refreshStarted.countDown();
                await(release);
            }
            return call;
        };

        final Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(slowOnRefresh, () -> Duration.ofMillis(50));

        assertThat(memoizedSupplier.get()).isEqualTo(1);
        Thread.sleep(100);

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final Future<Integer> refresher = executor.submit(memoizedSupplier::get);
            assertThat(refreshStarted.await(30, TimeUnit.SECONDS)).isTrue();

            // The refresh is stuck in the delegate, yet other callers get the stale value straight away
            final Future<Integer> other = executor.submit(memoizedSupplier::get);
            assertThat(other.get(30, TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(calls.get()).isEqualTo(2);

            release.countDown();
            assertThat(refresher.get(30, TimeUnit.SECONDS)).isEqualTo(2);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void shouldMeasureExpiryFromRefreshCompletion() throws InterruptedException {
        final AtomicInteger calls = new AtomicInteger();
        final Supplier<Integer> slow = () -> {
            sleep(300);
            return calls.incrementAndGet();
        };

        final Supplier<Integer> memoizedSupplier =
                CustomSuppliers.memoizeWithExpiration(slow, () -> Duration.ofMillis(400));

        assertThat(memoizedSupplier.get()).isEqualTo(1);

        // 500ms after the call began, but only 200ms after the value was produced
        Thread.sleep(200);

        assertThat(memoizedSupplier.get()).isEqualTo(1);
        assertThat(calls.get()).isEqualTo(1);
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

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}