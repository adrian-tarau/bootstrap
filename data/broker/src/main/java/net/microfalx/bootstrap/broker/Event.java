package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;

import java.time.LocalDateTime;

/**
 * An event produced by a {@link BrokerConsumer}
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public interface Event<K, V> extends Identifiable<String> {

    /**
     * Returns the key associated with the record
     *
     * @return the key, if available
     */
    K getKey();

    /**
     * Returns the value/data associated with the record
     *
     * @return the value
     */
    V getValue();

    /**
     * Returns the offset of the event inside the partition.
     *
     * @return the offset
     */
    PartitionOffset getOffset();

    /**
     * Returns the timestamp of the event (when the event was added to the topic).
     *
     * @return a non-null instance
     */
    LocalDateTime getTimestamp();
}
