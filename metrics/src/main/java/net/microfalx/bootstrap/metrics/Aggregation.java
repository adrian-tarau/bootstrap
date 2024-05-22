package net.microfalx.bootstrap.metrics;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An in-memory aggregator for metrics.
 * <p>
 * The default aggregation interval is 5 minutes.
 */
public final class Aggregation {

    private Type type = Type.SUM;
    private Duration step;
    private long stepAsMillis;
    private final Map<String, TimeSeries> timeSeries = new HashMap<>();

    public Aggregation() {
        setStep(Duration.ofMinutes(5));
    }

    /**
     * Returns the aggregation type.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Changes the aggregation type.
     *
     * @param type the new type
     * @return self
     */
    public Aggregation setType(Type type) {
        requireNonNull(type);
        checkStarted();
        this.type = type;
        return this;
    }

    /**
     * Returns the aggregation interval.
     *
     * @return a non-null instance
     */
    public Duration getStep() {
        return step;
    }

    /**
     * Changes the aggregation interval.
     *
     * @param step the step between points
     * @return a non-null instance
     */
    public Aggregation setStep(Duration step) {
        requireNonNull(step);
        if (step.toMillis() < 1000) throw new IllegalArgumentException("Step cannot be less than 1s");
        checkStarted();
        this.step = MetricUtils.round(step);
        this.stepAsMillis = step.toMillis();
        return this;
    }

    /**
     * Adds a new value for a metric to the aggregation.
     *
     * @param metric the metric
     * @param value  the value
     */
    public void add(Metric metric, Value value) {
        requireNonNull(metric);
        requireNonNull(value);
        getTimeSeries(metric).add(value);
    }

    /**
     * Merges to (partial) aggregations.
     *
     * @param aggregation the source aggregation
     */
    public void merge(Aggregation aggregation) {
        requireNonNull(aggregation);
        for (TimeSeries timeSeries : aggregation.timeSeries.values()) {
            getTimeSeries(timeSeries.metric).merge(timeSeries);
        }
    }

    /**
     * Returns the aggregations as {@link Matrix matrixes}.
     *
     * @return a non-null instance
     */
    public Collection<Matrix> toMatrixes() {
        return timeSeries.values().stream().map(TimeSeries::toMatrix).collect(Collectors.toList());
    }

    private void checkStarted() {
        if (!timeSeries.isEmpty()) {
            throw new IllegalStateException("The step cannot be changed after the aggregation started");
        }
    }

    private TimeSeries getTimeSeries(Metric metric) {
        return this.timeSeries.computeIfAbsent(metric.getHash(), s -> new TimeSeries(metric));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("step=" + step)
                .add("timeSeries=" + timeSeries)
                .toString();
    }

    /**
     * An enum for the aggregation type
     */
    public enum Type {

        /**
         * Values are added together.
         */
        SUM,

        /**
         * The minimum value is retained.
         */
        MIN,

        /**
         * The maximum value is retained.
         */
        MAX,

        /**
         * The average value is calculated
         */
        AVG
    }

    private class TimeSeries {

        private final Metric metric;
        private final Map<Long, Value> values = new HashMap<>();
        private final Map<Long, Integer> counts = new HashMap<>();

        public TimeSeries(Metric metric) {
            this.metric = metric;
        }

        private void add(Value value) {
            long timestamp = (value.getTimestamp() / stepAsMillis) * stepAsMillis;
            value = Value.create(timestamp, value.getValue());
            if (type == Type.AVG) {
                counts.merge(timestamp, 1, Integer::sum);
            }
            values.merge(timestamp, value, (oldValue, newValue) -> {
                return switch (type) {
                    case MIN -> oldValue.getValue() > newValue.getValue() ? newValue : oldValue;
                    case MAX -> oldValue.getValue() < newValue.getValue() ? newValue : oldValue;
                    case SUM, AVG -> oldValue.add(newValue.getValue());
                };
            });
        }

        private void merge(TimeSeries timeSeries) {
            if (!metric.equals(timeSeries.metric))
                throw new MetricException("Cannot merge two time-series with different metrics, " +
                        ", source: " + timeSeries.metric + ", target: " + metric);
            for (Value value : timeSeries.values.values()) {
                add(value);
            }
        }

        private Value change(Value value) {
            if (type == Type.AVG) {
                Integer count = counts.get(value.getTimestamp());
                return Value.create(value.getTimestamp(), value.getValue() / count);
            } else {
                return value;
            }
        }

        private Matrix toMatrix() {
            Iterator<Value> iterator = values.values().stream().sorted(Comparator.comparing(Value::getTimestamp))
                    .map(this::change).iterator();
            return Matrix.create(metric, iterator);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TimeSeries.class.getSimpleName() + "[", "]")
                    .add("metric=" + metric)
                    .add("values=" + values.size())
                    .add("counts=" + counts.size())
                    .toString();
        }
    }
}
