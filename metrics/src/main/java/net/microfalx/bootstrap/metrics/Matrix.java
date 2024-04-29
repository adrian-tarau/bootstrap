package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A matrix of values for a metric.
 */
@ToString
public class Matrix {

    private final Metric metric;
    private final List<Value> values;

    /**
     * Creates a matrix for a given metric and its values.
     *
     * @param metric the metric
     * @param values the values
     * @return a non-null instance
     */
    public static Matrix create(Metric metric, Iterator<Value> values) {
        return new Matrix(metric, CollectionUtils.toIterable(values));
    }

    /**
     * Creates a matrix for a given metric and its values.
     *
     * @param metric the metric
     * @param values the values
     * @return a non-null instance
     */
    public static Matrix create(Metric metric, Iterable<Value> values) {
        return new Matrix(metric, values);
    }

    Matrix(Metric metric, Iterable<Value> values) {
        requireNonNull(metric);
        this.metric = metric;
        this.values = CollectionUtils.toList(values);
    }

    /**
     * Returns the metric.
     *
     * @return a non-null instance
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Returns the values.
     *
     * @return a non-nll instance
     */
    public List<Value> getValues() {
        return unmodifiableList(values);
    }

    /**
     * Returns the number of points this matrix has.
     *
     * @return a positive integer
     */
    public int getCount() {
        return values.size();
    }

    /**
     * Returns first value in the matrix.
     *
     * @return an optional value
     */
    public Optional<Value> getFirst() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * Returns last (most recent) value in the matrix.
     *
     * @return the value, null if there are no values available
     */
    public Optional<Value> getLast() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(values.size() - 1));
    }

    /**
     * Returns the average across the interval.
     *
     * @return a optional average
     */
    public OptionalDouble getAverage() {
        return values.stream().mapToDouble(Value::asDouble).average();
    }

    /**
     * Returns the minimum across the interval.
     *
     * @return a optional average
     */
    public OptionalDouble getMinimum() {
        return values.stream().mapToDouble(Value::asDouble).min();
    }

    /**
     * Returns the maximum across the interval.
     *
     * @return a optional average
     */
    public OptionalDouble getMaximum() {
        return values.stream().mapToDouble(Value::asDouble).max();
    }
}
