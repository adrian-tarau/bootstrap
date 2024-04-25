package net.microfalx.bootstrap.broker;

import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A topic for a broker.
 */
public final class Topic implements Identifiable<String>, Nameable, Cloneable {

    private final String id;
    private final Broker broker;
    private final String name;

    private String subscription = "default";
    private String clientId = BrokerUtils.createClientId();
    private boolean autoCommit;

    private Format format = Format.JSON;
    private int maximumPollRecords = 500;
    private Duration timeout = Duration.ofSeconds(30);
    private OffsetResetStrategy offsetResetStrategy = OffsetResetStrategy.CURRENT;

    /**
     * Creates a topic.
     *
     * @param broker the broker
     * @param name   the topic name
     * @return a non-null instance
     */
    public static Topic create(Broker broker, String name) {
        return new Topic(broker, name);
    }

    Topic(Broker broker, String name) {
        requireNonNull(broker);
        requireNotEmpty(name);
        this.broker = broker;
        this.name = name;
        this.id = StringUtils.toIdentifier(broker.getId() + "_" + name);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the broker which owns the topic.
     *
     * @return a non-null instance
     */
    public Broker getBroker() {
        return broker;
    }

    /**
     * Returns the subscription of the consumer for this topic.
     *
     * @return a non-null instance
     */
    public String getSubscription() {
        return subscription;
    }

    /**
     * Creates a copy of this topic and changes the subscription for this topic.
     *
     * @param subscription the new subscription
     * @return a new instance
     */
    public Topic withSubscription(String subscription) {
        requireNotEmpty(subscription);
        Topic copy = copy();
        copy.subscription = subscription;
        return copy;
    }

    /**
     * Returns the client identifier.
     *
     * @return a non-null instance
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Creates a copy of this topic and changes the client identifier for this topic.
     *
     * @param clientId the new client identifier
     * @return a new instance
     */
    public Topic withClientId(String clientId) {
        requireNotEmpty(clientId);
        Topic copy = copy();
        copy.clientId = clientId;
        return copy;
    }

    /**
     * Returns the maximum number of records returned in a single call to poll().
     *
     * @return a positive integer
     */
    public int getMaximumPollRecords() {
        return maximumPollRecords;
    }

    /**
     * Creates a copy of this topic and changes the client identifier for this topic.
     *
     * @param maximumPollRecords the new maximum poll records
     * @return a new instance
     */
    public Topic withMaximumPollRecords(int maximumPollRecords) {
        requireNotEmpty(clientId);
        Topic copy = copy();
        copy.maximumPollRecords = maximumPollRecords;
        return copy;
    }

    /**
     * Returns whether the auto-commit is turned on.
     *
     * @return a non-null instance
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Creates a copy of this topic and changes the timeout for this topic.
     *
     * @param autoCommit {@code true} to auto-commit, {@code false} otherwise
     * @return a new instance
     */
    public Topic withAutoCommit(boolean autoCommit) {
        Topic copy = copy();
        copy.autoCommit = autoCommit;
        return copy;
    }

    /**
     * Returns the timeout for any consumer requests.
     *
     * @return a non-null instance
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Creates a copy of this topic and changes the timeout for this topic.
     *
     * @param timeout the new subscription
     * @return a new instance
     */
    public Topic withRequestTimeout(Duration timeout) {
        requireNotEmpty(timeout);
        Topic copy = copy();
        copy.timeout = timeout;
        return copy;
    }

    /**
     * Returns the timeout for any consumer requests.
     *
     * @return a non-null instance
     */
    public OffsetResetStrategy getOffsetResetStrategy() {
        return offsetResetStrategy;
    }

    /**
     * Creates a copy of this topic and changes the offset reset strategy for this topic.
     *
     * @param offsetResetStrategy the new subscription
     * @return a new instance
     */
    public Topic withOffsetResetStrategy(OffsetResetStrategy offsetResetStrategy) {
        requireNotEmpty(offsetResetStrategy);
        Topic copy = copy();
        copy.offsetResetStrategy = offsetResetStrategy;
        return copy;
    }

    /**
     * Returns the encoding format for any consumer requests.
     *
     * @return a non-null instance
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Creates a copy of this topic and changes the timeout for this topic.
     *
     * @param format the encoding format
     * @return a new instance
     */
    public Topic withFormat(Format format) {
        requireNotEmpty(format);
        Topic copy = copy();
        copy.format = format;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(id, topic.id) && Objects.equals(broker, topic.broker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, broker);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Topic.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("subscription='" + subscription + "'")
                .add("client='" + clientId + "'")
                .add("autoCommit=" + autoCommit)
                .add("format=" + format)
                .add("maximumPollRecords=" + maximumPollRecords)
                .add("timeout=" + timeout)
                .add("offsetResetStrategy=" + offsetResetStrategy)
                .add("broker=" + broker.getName())
                .toString();
    }

    private Topic copy() {
        try {
            return (Topic) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    /**
     * An status enum for a topic
     */
    public enum Status {

        /**
         * The topic is healthy, events can be consumed and published
         */
        HEALTHY,

        /**
         * The topic is healthy, but consumers are behind the consumers
         */
        LATE,

        /**
         * The topic cannot be processed due to errors
         */
        FAULTY;
    }

    /**
     * Holds the strategy on how offsets are handled.
     */
    public enum OffsetResetStrategy {

        /**
         * The consumer group starts from the latest committed event for the current subscription.
         */
        CURRENT,

        /**
         * The consumer group starts from the latest committed event (most recent events).
         */
        LATEST,

        /**
         * The consumer group starts from the earliest committed offset (the oldest events)
         */
        EARLIEST

    }

    /**
     * The encoding format for events
     */
    public enum Format {
        RAW,
        JSON,
        AVRO,
    }
}
