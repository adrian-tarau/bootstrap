package net.microfalx.bootstrap.broker.pulsar;

import net.microfalx.bootstrap.broker.Broker;
import net.microfalx.bootstrap.broker.BrokerException;
import net.microfalx.bootstrap.broker.Topic;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.HashMap;
import java.util.Map;

public class PulsarUtils {

    public static PulsarClient createClient(Topic topic) {
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

    private static Map<String, Object> createProperties(Topic topic) {
        Broker broker = topic.getBroker();
        final Map<String, Object> props = new HashMap<>(broker.getParameters());
        return props;
    }
}
