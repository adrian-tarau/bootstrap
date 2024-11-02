package net.microfalx.bootstrap.metrics;

import lombok.ToString;

/**
 * An instant value for a metric.
 */
@ToString
public final class Vector {

    private final Metric metric;
    private final Value value;

    /**
     * Creates an empty instance of a vector.
     *
     * @param metric the metric name
     * @return a non-null instance
     */
    public static Vector empty(Metric metric) {
        return new Vector(metric, Value.zero());
    }

    /**
     * Creates an instance of a vector.
     *
     * @param metric the metric name
     * @param value  the value
     * @return a non-null instance
     */
    public static Vector create(Metric metric, Value value) {
        return new Vector(metric, value);
    }

    Vector(Metric metric, Value value) {
        this.metric = metric;
        this.value = value;
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
     * Returns the value.
     *
     * @return a non-nll instance
     */
    public Value getValue() {
        return value;
    }

}
