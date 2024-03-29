package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.*;
import net.microfalx.lang.IOUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.StringUtils.isNotEmpty;

public class KafkaBrokerConsumer<K, V> extends BrokerConsumer<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBrokerConsumer.class);

    private static final ThreadLocal<Deserializer<?>> DESERIALIZER = new ThreadLocal<>();

    private Consumer<K, V> consumer;
    private volatile Set<TopicPartition> initialPartitions;
    private final Set<TopicPartition> currentPartitions = new CopyOnWriteArraySet<>();

    private final Map<Integer, Long> offsetPositions = new ConcurrentHashMap<>();
    private final Set<Integer> offsetPositionsApplied = new CopyOnWriteArraySet<>();

    public KafkaBrokerConsumer(Topic topic) {
        super(topic);
    }

    @Override
    public void initialize(Object... context) {
        Topic topic = getTopic();
        final Map<String, Object> props = createProperties();
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic.getName()), new ConsumerRebalanceListenerImpl());
    }

    @Override
    public void release() {
        IOUtils.closeQuietly(consumer);
    }

    @Override
    protected Collection<Event<K, V>> doPoll(Duration timeout) {
        Topic topic = getTopic();
        try {
            Collection<Event<K, V>> events = new ArrayList<>();
            ConsumerRecords<K, V> records = consumer.poll(timeout);
            for (ConsumerRecord<K, V> record : records) {
                events.add(new KafkaEvent<>(topic, record));
            }
            return events;
        } catch (Exception e) {
            throw new BrokerException("Failed to poll events from " + BrokerUtils.describe(topic), e);
        }
    }

    @Override
    protected void doCommit() {
        if (!getTopic().isAutoCommit()) {
            consumer.commitSync();
        } else {
            consumer.commitSync();
        }
    }

    @Override
    protected void doRollback() {

    }

    private Map<String, Object> createProperties() {
        Topic topic = getTopic();
        DESERIALIZER.set(new KafkaDeserializer<>(topic));
        final Map<String, Object> props = new HashMap<>(topic.getBroker().getParameters());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, topic.getSubscription());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, topic.getClientId());
        props.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, topic.getMaximumPollRecords());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DeserializerWrapper.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DeserializerWrapper.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, topic.isAutoCommit());
        String strategy = getOffsetResetStrategy(topic.getOffsetResetStrategy());
        if (isNotEmpty(strategy)) props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, strategy);
        return props;
    }

    private static String getOffsetResetStrategy(Topic.OffsetResetStrategy strategy) {
        return switch (strategy) {
            case LATEST -> "latest";
            case EARLIEST -> "earliest";
            case CURRENT -> null;
        };
    }

    void updatePartitions(Collection<TopicPartition> partitions, boolean revoked) {
        if (initialPartitions == null && !revoked) {
            initialPartitions = new CopyOnWriteArraySet<>();
        }
        if (revoked) {
            currentPartitions.removeAll(partitions);
        } else {
            currentPartitions.addAll(partitions);
            for (TopicPartition partition : partitions) {
                if (offsetPositionsApplied.add(partition.partition())) {
                    switch (getTopic().getOffsetResetStrategy()) {
                        case EARLIEST -> consumer.seekToBeginning(Collections.singleton(partition));
                        case LATEST -> consumer.seekToEnd(Collections.singleton(partition));
                        default -> {
                        }
                    }
                } else if (offsetPositions.containsKey(partition.partition())) {
                    consumer.seek(partition, offsetPositions.get(partition.partition()));
                }
            }
        }
    }

    private class ConsumerRebalanceListenerImpl implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            LOGGER.info("Partitions revoked from " + getTopic().getName() + ", partitions" + partitions);
            updatePartitions(partitions, true);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            LOGGER.info("Partitions assigned to " + getTopic().getName() + ", partitions" + partitions);
            updatePartitions(partitions, false);
        }

        @Override
        public void onPartitionsLost(Collection<TopicPartition> partitions) {
            LOGGER.info("Partitions lost from " + getTopic().getName() + ", partitions" + partitions);
            updatePartitions(partitions, true);
        }
    }


    public static class DeserializerWrapper<T> implements Deserializer<T> {

        private final Deserializer<T> deserializer;

        public DeserializerWrapper() {
            deserializer = (Deserializer<T>) DESERIALIZER.get();
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            return deserializer.deserialize(topic, data);
        }
    }
}
