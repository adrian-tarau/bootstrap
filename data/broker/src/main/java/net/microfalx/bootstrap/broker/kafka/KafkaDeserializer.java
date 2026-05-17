package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.BrokerException;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

class KafkaDeserializer<T> implements Deserializer<T> {

    private final Topic topic;
    private final Deserializer<T> deserializer;

    @SuppressWarnings("unchecked")
    KafkaDeserializer(Topic topic) {
        this.topic = topic;
        switch (topic.getFormat()) {
            case RAW -> deserializer = (Deserializer<T>) new ByteArrayDeserializer();
            case JSON -> deserializer = new JsonDeserializer<>();
            default -> {
                throw new BrokerException("Unsupported format '" + topic.getFormat() + "' for topic '" + this.topic.getName() + "'");
            }
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return deserializer.deserialize(topic, data);
    }
}
