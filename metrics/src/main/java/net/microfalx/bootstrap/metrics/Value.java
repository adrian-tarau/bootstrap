package net.microfalx.bootstrap.metrics;

import net.microfalx.lang.TimeUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochMilli;

/**
 * A class which holds a numeric value at a point in time.
 */
public final class Value {

    private final long timestamp;
    private final double value;

    /**
     * Creates an instance with the current timestamp and a value of zero.
     *
     * @return non-null instance
     */
    public static Value zero() {
        return create(currentTimeMillis(), 0);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @return non-null instance
     */
    public static Value create(LocalDateTime timestamp, double value) {
        return new Value(TimeUtils.toMillis(timestamp), value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @return non-null instance
     */
    public static Value create(long timestamp, double value) {
        return new Value(timestamp, value);
    }

    Value(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    /**
     * Returns the timestamp.
     *
     * @return millis since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the timestamp as an instance.
     *
     * @return a non-null instance
     */
    public Instant asInstant() {
        return ofEpochMilli(timestamp);
    }

    /**
     * Returns the timestamp as an date/time.
     *
     * @return a non-null instance
     */
    public ZonedDateTime asDateTime() {
        return ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
    }

    /**
     * Returns the value as a double type.
     *
     * @return the value
     */
    public double asDouble() {
        return value;
    }

    /**
     * Returns the value as a float type.
     *
     * @return the value
     */
    public float asFloat() {
        return (float) value;
    }

    /**
     * Returns the value as a long type.
     *
     * @return the value
     */
    public long asLong() {
        return (long) value;
    }

    /**
     * Returns the value as an int type.
     *
     * @return the value
     */
    public int asInt() {
        return (int) value;
    }

    /**
     * Creates a new value object and adds the value.
     *
     * @param value the value to add
     * @return a new instance
     */
    public Value add(double value) {
        return new Value(timestamp, this.value + value);
    }

    @Override
    public String toString() {
        return TimeUtils.toZonedDateTime(timestamp) + "=" + value;
    }
}
