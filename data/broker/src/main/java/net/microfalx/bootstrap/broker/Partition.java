package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.ObjectUtils;

import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A description of a topic partition.
 */
public final class Partition implements Identifiable<String>, Nameable, Comparable<Partition> {

    private final String id;
    private final Topic topic;
    private final Object value;

    /**
     * Creates a partition.
     *
     * @param topic the topic
     * @param value the partition identifier/value
     * @return a non-null instance
     */
    public static Partition create(Topic topic, Object value) {
        return new Partition(topic, value);
    }

    public Partition(Topic topic, Object value) {
        requireNonNull(topic);
        requireNonNull(value);
        this.topic = topic;
        this.value = value;
        this.id = toIdentifier(topic.getId() + "_" + ObjectUtils.toString(value));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return topic.getName() + " [" + value + "]";
    }

    @Override
    public int compareTo(Partition o) {
        return id.compareTo(o.id);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Partition.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("topic=" + topic)
                .add("value=" + value)
                .toString();
    }
}
