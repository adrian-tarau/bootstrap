package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.BrokerProducer;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaBrokerProducer<K, V> extends BrokerProducer<K, V> {

    private static final ThreadLocal<Serializer<?>> SERIALIZER = new ThreadLocal<>();

    public KafkaBrokerProducer(Topic topic) {
        super(topic);
    }

    @Override
    public void initialize(Object... context) {

    }

    @Override
    public void release() {

    }

    public static class SerializerWrapper<T> implements Serializer<T> {

        private Serializer<T> serializer;

        public SerializerWrapper() {
            serializer = (Serializer<T>) SERIALIZER.get();
        }

        @Override
        public byte[] serialize(String topic, T data) {
            return serializer.serialize(topic, data);
        }
    }
}
