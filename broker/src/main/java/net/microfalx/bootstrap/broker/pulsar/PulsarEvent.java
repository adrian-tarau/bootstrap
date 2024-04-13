package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.Event;
import net.microfalx.bootstrap.broker.Partition;
import net.microfalx.bootstrap.broker.PartitionOffset;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.lang.TimeUtils;
import org.apache.pulsar.client.api.Message;

import java.time.LocalDateTime;

class PulsarEvent<K, V> implements Event<K, V> {

    private final Topic topic;
    private final Message<V> message;

    PulsarEvent(Topic topic, Message<V> message) {
        this.topic = topic;
        this.message = message;
    }

    Message<V> getMessage() {
        return message;
    }

    @Override
    public String getId() {
        return message.getMessageId().toString();
    }

    @Override
    public K getKey() {
        return (K) message.getKey();
    }

    @Override
    public V getValue() {
        return message.getValue();
    }

    @Override
    public PartitionOffset getOffset() {
        return PartitionOffset.create(Partition.create(topic, 1), 1);
    }

    @Override
    public LocalDateTime getTimestamp() {
        return TimeUtils.toLocalDateTime(message.getPublishTime());
    }


}
