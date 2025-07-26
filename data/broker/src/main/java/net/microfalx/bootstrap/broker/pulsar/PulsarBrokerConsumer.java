package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.*;
import org.apache.pulsar.client.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.IOUtils.closeQuietly;

public class PulsarBrokerConsumer<K, V> extends BrokerConsumer<K, V> {

    private final PulsarClient client;
    private volatile Consumer<V> consumer;

    private final Queue<MessageId> pendingAcknowledge = new LinkedBlockingQueue<>();

    public PulsarBrokerConsumer(BrokerService brokerService, Topic topic, PulsarClient client) {
        super(brokerService, topic);
        requireNonNull(client);
        this.client = client;
    }

    @Override
    public long getLag() {
        return 0;
    }

    @Override
    public Collection<Partition> getPartitions() {
        return Collections.emptyList();
    }

    @Override
    protected Collection<Event<K, V>> doPoll(Duration timeout) {
        checkClosed();
        Collection<Event<K, V>> events = new ArrayList<>();
        boolean autoCommit = getTopic().isAutoCommit();
        int maximumPollRecords = getTopic().getMaximumPollRecords();
        long end = System.currentTimeMillis() + timeout.toMillis();
        while (end > System.currentTimeMillis()) {
            try {
                Message<V> message = doWithStatus(Status.POLL, (Callable<Message<V>>) () -> consumer.receive(500, TimeUnit.MILLISECONDS));
                if (message != null) {
                    doWithStatus(Status.CONSUME, (t) -> {
                        PulsarEvent<K, V> event = new PulsarEvent<>(getTopic(), message);
                        events.add(event);
                        if (!autoCommit) pendingAcknowledge.add(message.getMessageId());
                    });
                }
            } catch (Exception e) {
                handleException(e);
                throw new BrokerException("Failed to consume events from '" + getTopic().getName() + "'", e);
            }
            if (events.size() >= maximumPollRecords) break;
        }
        if (autoCommit) {
            for (Event<K, V> record : events) {
                acknowledge(((PulsarEvent<K, V>) record).getMessage().getMessageId());
            }
        }
        return unmodifiableCollection(events);
    }

    @Override
    protected void doCommit() {
        checkClosed();
        doWithStatus(Status.COMMIT, (t) -> {
            for (MessageId messageId : pendingAcknowledge) {
                acknowledge(messageId);
            }
            pendingAcknowledge.clear();
        });
    }

    @Override
    protected void doRollback() {
        checkClosed();
        doWithStatus(Status.ROLLBACK, (t) -> {
            pendingAcknowledge.clear();
            close();
            initialize();
        });
    }

    @Override
    protected void doInitialize(Object... context) {
        ConsumerBuilder<V> consumerBuilder = client.newConsumer(new PulsarSchema<>(getTopic()));
        consumerBuilder.topic(getTopic().getName());
        consumerBuilder.consumerName(getTopic().getClientId());
        consumerBuilder.subscriptionName(getTopic().getSubscription());
        consumerBuilder.autoUpdatePartitions(true).enableRetry(true).subscriptionMode(SubscriptionMode.Durable);
        consumerBuilder.subscriptionType(SubscriptionType.Shared);
        try {
            consumer = consumerBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new BrokerException("Failed to create consumer for '" + getTopic().getName() + "'", e);
        }
    }

    @Override
    protected void doRelease() {
        rollback();
        closeQuietly(consumer);
    }

    public void acknowledge(MessageId messageId) {
        try {
            consumer.acknowledge(messageId);
        } catch (PulsarClientException e) {
            handleException(e);
            throw new BrokerException("Failed to acknowledge event (" + messageId + ") from '" + getTopic().getName() + "'", e);
        }
    }
}
