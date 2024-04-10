package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.broker.BrokerUtils.METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

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
        brokerService.releaseProducer(this);
        METRICS.time("Release", (t) -> doRelease());
    }

    @Override
    public final void close() {
        release();
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
     * A status for the consumer
     */
    public enum Status {

        /**
         * The consumer tries to connect to the server.
         */
        CONNECT,

        /**
         * The consumer is connected, healthy and idle
         */
        IDLE,

        /**
         * The consumer is sending events.
         */
        SEND,

        /**
         * The consumer is committing.
         */
        COMMIT,

        /**
         * The consumer is rolling back.
         */
        ROLLBACK,
    }
}
