package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.*;
import net.microfalx.lang.annotation.Provider;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.HashMap;
import java.util.Map;

@Provider
public class PulsarBrokerProvider extends AbstractBrokerProvider {

    @Override
    public <K, V> BrokerConsumer<K, V> createConsumer(Topic topic) {
        return new PulsarBrokerConsumer<>(getBrokerService(), topic, createClient(topic));
    }

    @Override
    public <K, V> BrokerProducer<K, V> createProducer(Topic topic) {
        return new PulsarBrokerProducer<>(getBrokerService(), topic, createClient(topic));
    }

    @Override
    public boolean supports(Broker broker) {
        return broker.getType() == Broker.Type.PULSAR;
    }

    PulsarClient createClient(Topic topic) {
        Broker broker = topic.getBroker();
        try {
            ClientBuilder builder = PulsarClient.builder();
            final Map<String, Object> props = new HashMap<>(topic.getBroker().getParameters());
            builder.loadConf(createProperties(topic));
            return builder.build();
        } catch (PulsarClientException e) {
            throw new BrokerException("Failed to create a Pulsar client for broker " + broker.getId(), e);
        }
    }

    private Map<String, Object> createProperties(Topic topic) {
        Broker broker = topic.getBroker();
        final Map<String, Object> props = new HashMap<>(broker.getParameters());
        return props;
    }
}
