package net.microfalx.bootstrap.broker;

import net.microfalx.metrics.Metrics;

import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.formatMessage;

/**
 * Various broker utilities
 */
public class BrokerUtils {

    public static Metrics METRICS = Metrics.of("Broker");

    private static final AtomicInteger CLIENT_ID_GENERATOR = new AtomicInteger();

    /**
     * Describes a topic.
     *
     * @param topic the topic
     * @return a non-null instance
     */
    public static String describe(Topic topic) {
        requireNonNull(topic);
        return formatMessage("''{0}'' on ''{1}''", topic.getName(), topic.getBroker().getName());
    }

    /**
     * Creates a client identifier which uniquely identifies the consumer or producer.
     *
     * @return a non-null instance
     */
    public static String createClientId() {
        return createClientId("spring-boot");
    }

    /**
     * Creates a client identifier which uniquely identifies the consumer or producer.
     *
     * @return a non-null instance
     */
    public static String createClientId(String prefix) {
        String clientId = Long.toHexString(CLIENT_ID_GENERATOR.incrementAndGet());
        return prefix + "-" + clientId;
    }
}
