package net.microfalx.bootstrap.broker.kafka;

import net.microfalx.bootstrap.broker.*;
import net.microfalx.lang.annotation.Provider;

@Provider
public class KafkaBrokerProvider extends AbstractBrokerProvider {

    @Override
    public <K, V> BrokerConsumer<K, V> createConsumer(Topic topic) {
        return new KafkaBrokerConsumer<>(topic);
    }

    @Override
    public <K, V> BrokerProducer<K, V> createProducer(Topic topic) {
        return new KafkaBrokerProducer<>(topic);
    }

    @Override
    public boolean supports(Broker broker) {
        return broker.getType() == Broker.Type.KAFKA;
    }
}
