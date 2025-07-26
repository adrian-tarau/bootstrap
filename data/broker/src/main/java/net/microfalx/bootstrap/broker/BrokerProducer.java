package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.microfalx.bootstrap.broker.BrokerUtils.METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

/**
 * Base class for all producers.
 */
public abstract class BrokerProducer<K, V> implements Identifiable<String>, Initializable, Releasable {

    private final String id = UUID.randomUUID().toString();
    private final BrokerService brokerService;
    private final Topic topic;

    private final AtomicInteger commitCount = new AtomicInteger();
    private final AtomicInteger rollbackCount = new AtomicInteger();
    protected final AtomicInteger eventCount = new AtomicInteger();
    private final LocalDateTime createdAt = LocalDateTime.now();

    private volatile boolean closed;
    private volatile Status status = Status.IDLE;
    private volatile String lastFailure;

    public BrokerProducer(BrokerService brokerService, Topic topic) {
        requireNonNull(brokerService);
        requireNonNull(topic);
        this.brokerService = brokerService;
        this.topic = topic;
        brokerService.registerProducer(this);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final void initialize(Object... context) {
        METRICS.time("Initialize", (t) -> doInitialize(context));
    }

    @Override
    public final void release() {
        try {
            brokerService.releaseProducer(this);
            METRICS.time("Release", (t) -> doRelease());
        } finally {
            closed = true;
        }
    }

    @Override
    public final void close() {
        release();
    }

    /**
     * Returns whether the consumer was closed by the client.
     *
     * @return {@code true} if closed, {@code false} otherwise
     */
    public final boolean isClosed() {
        return closed;
    }

    /**
     * Returns the last failure of the consumer, if the consumer is in a failed state
     *
     * @return the last failure, null if consumer is healthy
     */
    public String getLastFailure() {
        return lastFailure;
    }

    /**
     * Sends an event to the topic.
     *
     * @param event the event
     */
    public final void send(V event) {
        send(null, event);
    }

    /**
     * Sends an event to the topic.
     *
     * @param key   the key (can be null)
     * @param event the event
     */
    public final void send(K key, V event) {
        METRICS.time("Send", (t) -> doSend(key, event));
    }

    /**
     * Commits the producer events if the auto-commit is off.
     */
    public final void commit() {
        commitCount.incrementAndGet();
        METRICS.time("Commit", (t) -> doCommit());
    }

    /**
     * Commits the producer events if the auto-commit is off.
     */
    public final void rollback() {
        rollbackCount.incrementAndGet();
        METRICS.time("Rollback", (t) -> doRollback());
    }

    /**
     * Returns the topic associated with the producer.
     *
     * @return a non-null instance
     */
    public final Topic getTopic() {
        return topic;
    }

    /**
     * Returns the number of commits executed by this producer.
     *
     * @return a positive integer
     */
    public int getCommitCount() {
        return commitCount.get();
    }

    /**
     * Returns the number of rollbacks executed by this producer.
     *
     * @return a positive integer
     */
    public int getRollbackCount() {
        return rollbackCount.get();
    }

    /**
     * Returns the number of events polled by this producer.
     *
     * @return a positive integer
     */
    public int getEventCount() {
        return eventCount.get();
    }

    /**
     * Returns the time when the consumer was created.
     *
     * @return a non-null instance
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the status of the producer.
     *
     * @return a non-null instance
     */
    public abstract Status getStatus();

    /**
     * Subclasses will implement this method to commit events.
     */
    protected abstract void doCommit();

    /**
     * Subclasses will implement this method to rollback events.
     */
    protected abstract void doRollback();

    /**
     * Initializes consumer resources.
     */
    protected abstract void doInitialize(Object... context);

    /**
     * Releases consumer resources.
     */
    protected abstract void doRelease();

    /**
     * Sends an event to the topic.
     *
     * @param key   the key (can be null)
     * @param event the event
     */
    protected abstract void doSend(K key, V event);

    /**
     * Checks whether the consumer was closed and throws an exception if true.
     */
    protected final void checkClosed() {
        if (closed) throw new BrokerException("Consumer '" + BrokerUtils.describe(topic) + "' is closed ");
    }

    /**
     * Calls a supplier and updates the status while the supplier runs.
     *
     * @param status   the status
     * @param callable the supplier
     * @param <T>      the return type
     * @return the return value
     */
    protected final <T> T doWithStatus(Status status, Callable<T> callable) throws Exception {
        updateStatus(status);
        try {
            return callable.call();
        } finally {
            updateStatus(Status.IDLE);
        }
    }

    /**
     * Calls a supplier and updates the status while the supplier runs.
     *
     * @param status   the status
     * @param supplier the supplier
     * @param <T>      the return type
     * @return the return value
     */
    protected final <T> T doWithStatus(Status status, Supplier<T> supplier) {
        updateStatus(status);
        try {
            return supplier.get();
        } finally {
            updateStatus(Status.IDLE);
        }
    }

    /**
     * Calls a consumer and updates the status while the supplier runs.
     *
     * @param status   the status
     * @param consumer the supplier
     * @param <T>      the return type
     * @return the return value
     */
    protected final <T> void doWithStatus(Status status, Consumer<T> consumer) {
        updateStatus(status);
        try {
            consumer.accept(null);
        } finally {
            updateStatus(Status.IDLE);
        }
    }

    /**
     * Updates the consumer status.
     *
     * @param status the new status
     */
    protected final void updateStatus(Status status) {
        this.status = status == null ? Status.IDLE : status;
        if (this.status != Status.FAILED) this.lastFailure = null;
    }

    /**
     * Handles an exception in the consumer.
     *
     * @param throwable the exception
     */
    protected final void handleException(Throwable throwable) {
        updateStatus(Status.FAILED);
        this.lastFailure = getRootCauseDescription(throwable);
    }

    /**
     * A status for the consumer
     */
    public enum Status {

        /**
         * The producer tries to connect to the server.
         */
        CONNECT,

        /**
         * The producer is connected, healthy and idle
         */
        IDLE,

        /**
         * The producer is sending events.
         */
        SEND,

        /**
         * The producer is committing.
         */
        COMMIT,

        /**
         * The producer is rolling back.
         */
        ROLLBACK,

        /**
         * The producer had an error
         */
        FAILED
    }
}
