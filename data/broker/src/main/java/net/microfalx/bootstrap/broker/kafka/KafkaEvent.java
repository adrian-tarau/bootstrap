package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.Event;
import net.microfalx.bootstrap.broker.Partition;
import net.microfalx.bootstrap.broker.PartitionOffset;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.lang.TimeUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.LocalDateTime;

class KafkaEvent<K, V> implements Event<K, V> {

    private final Topic topic;
    private final ConsumerRecord<K, V> record;

    KafkaEvent(Topic topic, ConsumerRecord<K, V> record) {
        this.topic = topic;
        this.record = record;
    }

    @Override
    public String getId() {
        return record.partition() + ":" + record.offset();
    }

    @Override
    public K getKey() {
        return record.key();
    }

    @Override
    public V getValue() {
        return record.value();
    }

    @Override
    public PartitionOffset getOffset() {
        return PartitionOffset.create(Partition.create(topic, record.partition()), record.offset());
    }

    @Override
    public LocalDateTime getTimestamp() {
        return TimeUtils.toLocalDateTime(record.timestamp());
    }


}
