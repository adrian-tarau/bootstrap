package net.microfalx.bootstrap.broker;

/**
 * An interface which can create consumers and producers.
 */
public interface BrokerProvider {

    BrokerService getBrokerService();

    /**
     * Creates a consumer.
     *
     * @param topic the topic
     * @return a non-null instance
     */
    <K, V> BrokerConsumer<K, V> createConsumer(Topic topic);

    /**
     * Creates a producer.
     *
     * @param topic the topic
     * @return a non-null instance
     */
    <K, V> BrokerProducer<K, V> createProducer(Topic topic);

    /**
     * Returns whether the broker is supported by this provider.
     *
     * @param broker the broker
     * @return {@code true} if broker is supported, {@code false} otherwise
     */
    boolean supports(Broker broker);
}
