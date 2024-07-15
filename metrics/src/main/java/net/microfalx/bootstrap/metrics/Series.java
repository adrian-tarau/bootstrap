package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A collection of point in time for a named series.
 */
@ToString
public final class Series implements Identifiable<String>, Nameable {

    private final String id = MetricUtils.nextId("series");
    private final String name;
    private final List<Value> values;

    private OptionalDouble average;
    private OptionalDouble minimum;
    private OptionalDouble maximum;

    private double weight = Double.MIN_VALUE;

    /**
     * Creates an empty series for a given metric and its values.
     *
     * @param name the name of the series
     * @return a non-null instance
     */
    public static Series create(String name) {
        return new Series(name, null);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param values the iterable
     * @return a non-null instance
     */
    public static Series create(String name, Iterable<Value> values) {
        return new Series(name, values);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param values the values
     * @return a non-null instance
     */
    public static Series create(String name, Value... values) {
        return new Series(name, Arrays.asList(values));
    }

    /**
     * Creates a series with random values.
     *
     * @param name     the name of the series.
     * @param start    the start time
     * @param interval the interval
     * @param count    the number of values to add
     * @param min      the minimum value
     * @param max      the maximum value
     * @return the series
     */
    public static Series random(String name, LocalDateTime start, Duration interval, int count, double min, double max) {
        Random random = ThreadLocalRandom.current();
        List<Value> values = new ArrayList<>();
        double range = max - min;
        for (int i = 0; i < count; i++) {
            values.add(Value.create(start, min + random.nextDouble(range)));
            start = start.plus(interval);
        }
        return Series.create(name, values);
    }

    Series(String name, Iterable<Value> values) {
        requireNonNull(name);
        this.name = name;
        this.values = toList(values);
        this.values.sort(Comparator.comparing(Value::getTimestamp));
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
     * Return a list of values.
     *
     * @return a non-null instance
     */
    public List<Value> getValues() {
        return unmodifiableList(values);
    }

    /**
     * Returns the number of points this series has.
     *
     * @return a positive integer
     */
    public int getCount() {
        return values.size();
    }

    /**
     * Returns first value in the series.
     *
     * @return an optional value
     */
    public Optional<Value> getFirst() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * Returns last (most recent) value in the series.
     *
     * @return the value, null if there are no values available
     */
    public Optional<Value> getLast() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(values.size() - 1));
    }

    /**
     * Returns the average across in the series.
     *
     * @return a optional average
     */
    public OptionalDouble getAverage() {
        if (average == null) {
            average = values.stream().mapToDouble(Value::asDouble).average();
        }
        return average;
    }

    /**
     * Returns the minimum across in the series.
     *
     * @return a optional average
     */
    public OptionalDouble getMinimum() {
        if (minimum == null) {
            minimum = values.stream().mapToDouble(Value::asDouble).min();
        }
        return minimum;
    }

    /**
     * Returns the maximum across in the series.
     *
     * @return a optional average
     */
    public OptionalDouble getMaximum() {
        if (maximum == null) {
            maximum = values.stream().mapToDouble(Value::asDouble).max();
        }
        return maximum;
    }

    /**
     * Returns the weight of this series.
     * <p>
     * The weight is useful to compare which series has more "stuff" and can be display first (or kept).
     *
     * @return a positive number
     */
    public double getWeight() {
        if (weight == Double.MIN_VALUE) {
            weight = getMaximum().orElse(0) / values.size();
        }
        return weight;
    }
}
