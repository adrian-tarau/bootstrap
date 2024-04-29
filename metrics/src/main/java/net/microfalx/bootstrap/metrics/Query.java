package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;

import java.time.Duration;
import java.time.ZonedDateTime;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * A query to extract time series from a repository
 * <p>
 * If no time reference is passed, the query selects the last 24.
 */
@ToString
public class Query implements Cloneable {

    private final String type;
    private final String text;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Duration step;
    private Duration timeOut = ofSeconds(30);

    /**
     * Creates an empty query.
     *
     * @param type the type of query
     * @return a non-null instance
     */
    public static Query create(String type) {
        return new Query(type, EMPTY_STRING);
    }

    /**
     * Creates a query
     *
     * @param type the type of query
     * @param text the query text
     * @return a non-null instance
     */
    public static Query create(String type, String text) {
        return new Query(type, text);
    }

    Query(String type, String text) {
        requireNotEmpty(type);
        this.type = type;
        this.text = defaultIfEmpty(text, EMPTY_STRING);
    }

    /**
     * Returns the type of query.
     * <p>
     * Each type identifies a target repository.
     *
     * @return a non-null instance
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the query text, used to select metrics and their time-series.
     *
     * @return the query, empty if no query
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the start time.
     * <p>
     * If a start time is not provided, the last 24h is selected.
     *
     * @return a non-null instance
     */
    public ZonedDateTime getStartTime() {
        return startTime != null ? startTime : ZonedDateTime.now().minusHours(24);
    }

    /**
     * Changes the start time.
     *
     * @param startTime the start time
     * @return a new instance with a different start time
     */
    public Query withStartTime(ZonedDateTime startTime) {
        requireNonNull(startTime);
        Query copy = copy();
        copy.startTime = startTime;
        return copy;
    }

    /**
     * Returns the end time.
     * <p>
     * If a start time is not provided, {@code now} is selected.
     *
     * @return a non-null instance
     */
    public ZonedDateTime getEndTime() {
        return endTime != null ? endTime : ZonedDateTime.now();
    }

    /**
     * Changes the end time.
     *
     * @param endTime the end time
     * @return a new instance with a different end time
     */
    public Query withEnd(ZonedDateTime endTime) {
        requireNonNull(endTime);
        Query copy = copy();
        copy.endTime = endTime;
        return copy;
    }

    /**
     * Returns the steps between returned points.
     * <p>
     * The step represents the aggregation interval. If not provided, it will be selected based on the time interval
     * to not return more than 50 steps per time interval.
     *
     * @return a non-null instance
     */
    public Duration getStep() {
        if (step == null) {
            Duration duration = Duration.between(getStartTime(), getEndTime());
            return MetricUtils.round(duration.dividedBy(50));
        }
        return step;
    }

    /**
     * Changes the step for this query.
     *
     * @param step the step
     * @return a new instance with a different step
     */
    public Query withStep(Duration step) {
        requireNonNull(step);
        Query copy = copy();
        copy.step = MetricUtils.round(step);
        return copy;
    }

    /**
     * Returns the execution timeout for this query.
     *
     * @return a non-null instance
     */
    public Duration getTimeOut() {
        return timeOut;
    }

    /**
     * Changes the execution timeout for this query.
     *
     * @param timeOut the new timeout
     * @return a new instance with a different timeout
     */
    public Query withTimeOut(Duration timeOut) {
        requireNonNull(timeOut);
        Query copy = copy();
        copy.timeOut = timeOut;
        return copy;
    }

    private Query copy() {
        try {
            return (Query) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }
}
