package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.*;
import net.microfalx.lang.annotation.Provider;

@Provider
public class PulsarBrokerProvider extends AbstractBrokerProvider {

    @Override
    public <K, V> BrokerConsumer<K, V> createConsumer(Topic topic) {
        return new PulsarBrokerConsumer<>(getBrokerService(), topic, PulsarUtils.createClient(topic));
    }

    @Override
    public <K, V> BrokerProducer<K, V> createProducer(Topic topic) {
        return new PulsarBrokerProducer<>(getBrokerService(), topic, PulsarUtils.createClient(topic));
    }

    @Override
    public boolean supports(Broker broker) {
        return broker.getType() == Broker.Type.PULSAR;
    }

}
