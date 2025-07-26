package net.microfalx.bootstrap.metrics.util;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An implementation of a {@link StatisticalSummary} which only cares about average, min and max.
 */
public class SimpleStatisticalSummary implements StatisticalSummary {

    private long n = 0;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    public SimpleStatisticalSummary() {
    }

    public SimpleStatisticalSummary(StatisticalSummary summary) {
        requireNonNull(summary);
        this.n = summary.getN();
        this.sum = summary.getSum();
        this.max = summary.getMax();
        this.min = summary.getMin();
    }

    @Override
    public double getMean() {
        return n == 0 ? 0 : sum / (double) n;
    }

    @Override
    public double getVariance() {
        return Double.NaN;
    }

    @Override
    public double getStandardDeviation() {
        return Double.NaN;
    }

    @Override
    public double getMax() {
        return max;
    }

    public SimpleStatisticalSummary setMax(double max) {
        this.max = max;
        return this;
    }

    @Override
    public double getMin() {
        return min;
    }

    public SimpleStatisticalSummary setMin(double min) {
        this.min = min;
        return this;
    }

    @Override
    public long getN() {
        return n;
    }

    public SimpleStatisticalSummary setN(long n) {
        this.n = n;
        return this;
    }

    @Override
    public double getSum() {
        return sum;
    }

    public SimpleStatisticalSummary setSum(double sum) {
        this.sum = sum;
        return this;
    }

    /**
     * Adds a new value to the statistics.
     *
     * @param value the value
     */
    public void add(double value) {
        this.sum = value;
        this.n++;
        this.min = Math.min(this.min, value);
        this.max = Math.max(this.max, value);
    }

    /**
     * Updates the summary by adding all the relevant values from another summary (count, sum, min and max).
     *
     * @param summary the value
     */
    public void add(StatisticalSummary summary) {
        if (summary.getN() == 0) return;
        this.sum += summary.getSum();
        this.n += summary.getN();
        this.min = Math.min(this.min, summary.getMin());
        this.max = Math.max(this.max, summary.getMax());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleStatisticalSummary that = (SimpleStatisticalSummary) o;
        return n == that.n && Double.compare(that.sum, sum) == 0 && Double.compare(that.min, min) == 0
                && Double.compare(that.max, max) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, sum, min, max);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SimpleStatisticalSummary.class.getSimpleName() + "[", "]")
                .add("n=" + n)
                .add("sum=" + sum)
                .add("min=" + min)
                .add("max=" + max)
                .toString();
    }
}
