package net.microfalx.bootstrap.test;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A helper class which provides task execution hardness and tracking during unit tests.
 */
public class TaskTracker {

    private final Map<Class<?>, AtomicInteger> executions = new ConcurrentHashMap<>();
    private final Map<Class<?>, AtomicInteger> submits = new ConcurrentHashMap<>();
    private final Map<Class<?>, AtomicInteger> schedules = new ConcurrentHashMap<>();

    /**
     * Returns the number of executions for all task.
     *
     * @return a positive integer
     */
    public int getExecutionCounts() {
        return executions.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    /**
     * Returns the number of executions for a given task type.
     *
     * @param type the task class
     * @return a positive integer
     */
    public int getExecutionCount(Class<?> type) {
        requireNonNull(type);
        AtomicInteger counter = executions.get(type);
        return counter == null ? 0 : counter.get();
    }

    /**
     * Returns the number of submits for a given task type.
     *
     * @param type the task class
     * @return a positive integer
     */
    public int getSubmitCount(Class<?> type) {
        requireNonNull(type);
        AtomicInteger counter = submits.get(type);
        return counter == null ? 0 : counter.get();
    }

    /**
     * Returns the number of submits for a given task type.
     *
     * @param type the task class
     * @return a positive integer
     */
    public int getScheduleCount(Class<?> type) {
        requireNonNull(type);
        AtomicInteger counter = schedules.get(type);
        return counter == null ? 0 : counter.get();
    }

    /**
     * Registers the execution of a runnable
     *
     * @param runnable the runnable
     */
    public void registerExecution(Runnable runnable) {
        requireNonNull(runnable);
        executions.computeIfAbsent(runnable.getClass(), k -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Registers the submission of a task.
     *
     * @param task the task
     * @return the future
     */
    public Future<?> registerSubmit(Object task) {
        requireNonNull(task);
        submits.computeIfAbsent(task.getClass(), k -> new AtomicInteger()).incrementAndGet();
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Registers the scheduling of a task
     *
     * @param task the task
     * @return the future
     */
    public Future<?> registerSchedule(Object task) {
        requireNonNull(task);
        schedules.computeIfAbsent(task.getClass(), k -> new AtomicInteger()).incrementAndGet();
        return new TestScheduledFuture<>(CompletableFuture.completedFuture(null));
    }

    private static class TestScheduledFuture<V> implements ScheduledFuture<V> {

        private final Future<V> future;
        private final Duration delay = Duration.ofMillis(500);

        TestScheduledFuture(Future<V> future) {
            this.future = future;
        }

        @Override
        public long getDelay(@NonNull TimeUnit unit) {
            return unit.convert(delay.toNanos(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(@NonNull Delayed o) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public V get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }
    }
}
