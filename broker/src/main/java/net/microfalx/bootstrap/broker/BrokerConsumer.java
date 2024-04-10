package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.broker.BrokerUtils.METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all consumers.
 */
public abstract class BrokerConsumer<K, V> implements Identifiable<String>, Initializable, Releasable {

    private final String id = UUID.randomUUID().toString();
    private final BrokerService brokerService;
    private final Topic topic;
    private final AtomicInteger commitCount = new AtomicInteger();
    private final AtomicInteger rollbackCount = new AtomicInteger();
    protected final AtomicInteger eventCount = new AtomicInteger();
    private final AtomicInteger pollCount = new AtomicInteger();

    private final LocalDateTime createdAt = LocalDateTime.now();

    public BrokerConsumer(BrokerService brokerService, Topic topic) {
        requireNonNull(brokerService);
        requireNonNull(topic);
        this.brokerService = brokerService;
        this.topic = topic;
        brokerService.registerConsumer(this);
    }

    @Override
    public final String getId() {
        return id;
    }

    /**
     * Returns the topic associated with the consumer.
     *
     * @return a non-null instance
     */
    public final Topic getTopic() {
        return topic;
    }

    /**
     * Polls for the next collection of events with a default timeout.
     *
     * @return a non-null instance
     */
    public final Collection<Event<K, V>> poll() {
        return poll(Duration.ofSeconds(5));
    }

    /**
     * Polls for the next collection of events.
     *
     * @param timeout the timeout
     * @return a non-null instance
     */
    public final Collection<Event<K, V>> poll(Duration timeout) {
        pollCount.incrementAndGet();
        return METRICS.time("Poll", () -> doPoll(timeout));
    }

    /**
     * Commits the consumes events if the auto-commit is off.
     */
    public final void commit() {
        commitCount.incrementAndGet();
        METRICS.time("Commit", (t) -> doCommit());
    }

    /**
     * Commits the consumes events if the auto-commit is off.
     */
    public final void rollback() {
        rollbackCount.incrementAndGet();
        METRICS.time("Rollback", (t) -> doRollback());
    }

    @Override
    public final void initialize(Object... context) {
        METRICS.time("Initialize", (t) -> doInitialize(context));
    }

    @Override
    public final void release() {
        brokerService.registerConsumer(this);
        METRICS.time("Release", (t) -> doRelease());
    }

    @Override
    public final void close() {
        release();
    }

    /**
     * Returns the number of commits executed by this consumer.
     *
     * @return a positive integer
     */
    public int getCommitCount() {
        return commitCount.get();
    }

    /**
     * Returns the number of rollbacks executed by this consumer.
     *
     * @return a positive integer
     */
    public int getRollbackCount() {
        return rollbackCount.get();
    }

    /**
     * Returns the number of events polled by this consumer.
     *
     * @return a positive integer
     */
    public int getEventCount() {
        return eventCount.get();
    }

    /**
     * Returns the number of polls executed by this consumer.
     *
     * @return a positive integer
     */
    public int getPollCount() {
        return pollCount.get();
    }

    /**
     * Returns the number of events this consumer still needs to process to become up-to-date by this consumer.
     *
     * @return a positive integer
     */
    public abstract long getLag();

    /**
     * Returns the status of the consumer.
     *
     * @return a non-null instance
     */
    public abstract Status getStatus();

    /**
     * Returns the partitions assigned to this consumer.
     *
     * @return a non-null instance
     */
    public abstract Collection<Partition> getPartitions();

    /**
     * Returns the time when the consumer was created.
     *
     * @return a non-null instance
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Subclasses will implement this method to poll for events.
     *
     * @param timeout the timeout
     * @return a non-null instance
     */
    protected abstract Collection<Event<K, V>> doPoll(Duration timeout);

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
         * The consumer is polling for events.
         */
        POLL,

        /**
         * The consumer is consuming polled events
         */
        CONSUME,

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
