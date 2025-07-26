package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.BrokerProducer;
import net.microfalx.bootstrap.broker.BrokerService;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.pulsar.client.api.PulsarClient;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class PulsarBrokerProducer<K, V> extends BrokerProducer<K, V> {

    private final PulsarClient client;

    public PulsarBrokerProducer(BrokerService brokerService, Topic topic, PulsarClient client) {
        super(brokerService, topic);
        requireNonNull(client);
        this.client = client;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    protected void doCommit() {
        checkClosed();
    }

    @Override
    protected void doRollback() {
        checkClosed();
    }

    @Override
    protected void doInitialize(Object... context) {

    }

    @Override
    protected void doRelease() {

    }

    @Override
    protected void doSend(K key, V event) {
        checkClosed();
    }
}
