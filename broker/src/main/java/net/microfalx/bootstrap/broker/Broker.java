package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.ObjectUtils;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.capitalizeWords;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A class which carries information about a broker.
 */
public class Broker implements Identifiable<String>, Nameable, Cloneable {

    private final String id;
    private final Type type;
    private String name;
    private ZoneId timeZone;
    private Map<String, String> parameters = new HashMap<>();

    public static Builder builder(Type type, String id) {
        return new Builder(type, id);
    }

    Broker(Type type, String id, String name) {
        requireNonNull(type);
        requireNonNull(id);
        this.type = type;
        this.id = id;
        this.name = name;
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
     * Returns the type of broker.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the time zone of the broker.
     *
     * @return a non-null instance
     */
    public ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Returns the parameters to connect to broker (and create consumers/producers).
     *
     * @return a non-null instance
     */
    public Map<String, String> getParameters() {
        return unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Broker.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("name='" + name + "'")
                .add("timeZone=" + timeZone)
                .add("parameters=" + parameters.size())
                .toString();
    }

    /**
     * An enum for a broker type
     */
    public enum Type {

        /**
         * <a href="https://kafka.apache.org/documentation/">Apache Kafka</a>
         */
        KAFKA,

        /**
         * <a href="https://pulsar.apache.org/docs/3.2.x/">Apache Pulsar</a>
         */
        PULSAR,

        /**
         * <a href="https://www.rabbitmq.com/docs/documentation">Rabbit MQ</a>
         */
        RABBITMQ
    }

    public static class Builder {

        private final String id;
        private final Type type;
        private String name;
        private ZoneId timeZone = ZoneId.systemDefault();
        private Map<String, String> parameters = new HashMap<>();

        public Builder(Type type, String id) {
            requireNonNull(type);
            requireNonNull(id);
            this.type = type;
            this.id = toIdentifier(id);
            this.name = capitalizeWords(id);
        }

        public Builder name(String name) {
            requireNotEmpty(name);
            this.name = name;
            return this;
        }

        public Builder timeZone(ZoneId zoneId) {
            requireNonNull(zoneId);
            this.timeZone = zoneId;
            return this;
        }

        public Builder parameter(String name, String value) {
            requireNonNull(name);
            parameters.put(name, value);
            return this;
        }

        public Builder parameters(Map<String, ?> parameters) {
            requireNonNull(parameters);
            for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                this.parameters.put(entry.getKey(), ObjectUtils.toString(entry.getValue()));
            }
            return this;
        }

        public Broker build() {
            Broker broker = new Broker(type, id, name);
            broker.timeZone = timeZone;
            broker.parameters.putAll(parameters);
            return broker;
        }
    }
}
