package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all producers.
 */
public abstract class BrokerProducer<K, V> implements Initializable, Releasable {

    private final Topic topic;

    public BrokerProducer(Topic topic) {
        requireNonNull(topic);
        this.topic = topic;
    }

    /**
     * Returns the topic associated with the producer.
     *
     * @return a non-null instance
     */
    public final Topic getTopic() {
        return topic;
    }
}
