package net.microfalx.bootstrap.broker.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import net.microfalx.bootstrap.broker.BrokerException;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

class KafkaDeserializer<T> implements Deserializer<T> {

    private final Topic topic;
    private final Deserializer<T> deserializer;

    KafkaDeserializer(Topic topic) {
        this.topic = topic;
        switch (topic.getFormat()) {
            case RAW -> deserializer = (Deserializer<T>) new ByteArrayDeserializer();
            case JSON -> deserializer = new JsonDeserializer<>();
            case AVRO -> deserializer = (Deserializer<T>) new KafkaAvroDeserializer();
            default -> throw new BrokerException("Unsupported format: " + topic.getFormat());
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return deserializer.deserialize(topic, data);
    }
}
