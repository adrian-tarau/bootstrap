package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import java.time.Duration;
import java.util.Collection;

import static net.microfalx.bootstrap.broker.BrokerUtils.METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all consumers.
 */
public abstract class BrokerConsumer<K, V> implements Initializable, Releasable {

    private final Topic topic;

    public BrokerConsumer(Topic topic) {
        requireNonNull(topic);
        this.topic = topic;
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
        return poll(Duration.ofSeconds(1000));
    }

    /**
     * Polls for the next collection of events.
     *
     * @param timeout the timeout
     * @return a non-null instance
     */
    public final Collection<Event<K, V>> poll(Duration timeout) {
        return METRICS.time("Poll", () -> doPoll(timeout));
    }

    /**
     * Commits the consumes events if the auto-commit is off.
     */
    public final void commit() {
        METRICS.time("Commit", (t) -> doCommit());
    }

    /**
     * Commits the consumes events if the auto-commit is off.
     */
    public final void rollback() {
        METRICS.time("Rollback", (t) -> doRollback());
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


}
