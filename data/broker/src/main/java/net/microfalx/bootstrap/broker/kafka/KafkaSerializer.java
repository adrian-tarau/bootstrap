package net.microfalx.bootstrap.broker.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import net.microfalx.bootstrap.broker.BrokerException;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

class KafkaSerializer<T> implements Serializer<T> {

    private final Topic topic;
    private final Serializer<T> serializer;

    KafkaSerializer(Topic topic) {
        this.topic = topic;
        switch (topic.getFormat()) {
            case RAW -> serializer = (Serializer<T>) new ByteArraySerializer();
            case JSON -> serializer = new JsonSerializer<>();
            case AVRO -> serializer = (Serializer<T>) new KafkaAvroSerializer();
            default -> throw new BrokerException("Unsupported format: " + topic.getFormat());
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        return serializer.serialize(topic, data);
    }
}
