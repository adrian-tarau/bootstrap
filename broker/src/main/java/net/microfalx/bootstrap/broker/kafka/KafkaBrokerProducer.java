package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.BrokerProducer;
import net.microfalx.bootstrap.broker.BrokerService;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.lang.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaBrokerProducer<K, V> extends BrokerProducer<K, V> {

    private static final ThreadLocal<Serializer<?>> SERIALIZER = new ThreadLocal<>();

    private volatile Producer<K, V> producer;

    public KafkaBrokerProducer(BrokerService brokerService, Topic topic) {
        super(brokerService, topic);
    }

    @Override
    public void doInitialize(Object... context) {
        Topic topic = getTopic();
        final Map<String, Object> props = createProperties();
        producer = new KafkaProducer<>(props);
    }

    @Override
    public void doRelease() {
        IOUtils.closeQuietly(producer);
    }

    @Override
    public Status getStatus() {
        return Status.IDLE;
    }

    @Override
    protected void doSend(K key, V event) {
checkClosed();
    }

    @Override
    protected void doCommit() {
        checkClosed();
        if (getTopic().isAutoCommit()) {
            producer.flush();
        } else {
            producer.commitTransaction();
        }
    }

    @Override
    protected void doRollback() {
        checkClosed();
        if (!getTopic().isAutoCommit()) {
            producer.commitTransaction();
        }
    }

    private Map<String, Object> createProperties() {
        Topic topic = getTopic();
        SERIALIZER.set(new KafkaSerializer<>(topic));
        final Map<String, Object> props = new HashMap<>(topic.getBroker().getParameters());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, topic.getClientId());
        props.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, topic.getMaximumPollRecords());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, SerializerWrapper.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SerializerWrapper.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, topic.isAutoCommit());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return props;
    }

    public static class SerializerWrapper<T> implements Serializer<T> {

        private final Serializer<T> serializer;

        public SerializerWrapper() {
            serializer = (Serializer<T>) SERIALIZER.get();
        }

        @Override
        public byte[] serialize(String topic, T data) {
            return serializer.serialize(topic, data);
        }
    }
}
