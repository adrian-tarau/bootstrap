package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.Nameable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A collection of point in time for a named series.
 */
@ToString
public final class Series implements Nameable {

    private final String name;
    private final List<Value> values;

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

    Series(String name, Iterable<Value> values) {
        requireNonNull(name);
        this.name = name;
        this.values = toList(values);
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
        return values.stream().mapToDouble(Value::asDouble).average();
    }

    /**
     * Returns the minimum across in the series.
     *
     * @return a optional average
     */
    public OptionalDouble getMinimum() {
        return values.stream().mapToDouble(Value::asDouble).min();
    }

    /**
     * Returns the maximum across in the series.
     *
     * @return a optional average
     */
    public OptionalDouble getMaximum() {
        return values.stream().mapToDouble(Value::asDouble).max();
    }
}
