package net.microfalx.bootstrap.metrics.util;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class MemoryStatisticalSummary implements StatisticalSummary {

    private long count;
    private double sum;
    private double min;
    private double max;
    private double mean;
    private double variance;
    private double stdDev;

    public MemoryStatisticalSummary() {
    }

    public MemoryStatisticalSummary(StatisticalSummary summary) {
        requireNonNull(summary);
        this.count = summary.getN();
        this.mean = summary.getMean();
        this.variance = summary.getVariance();
        this.stdDev = summary.getStandardDeviation();
        this.count = summary.getN();
        this.max = summary.getMax();
        this.min = summary.getMin();
        this.sum = summary.getSum();
    }

    @Override
    public double getMean() {
        return mean;
    }

    public MemoryStatisticalSummary setMean(double mean) {
        this.mean = mean;
        return this;
    }

    @Override
    public double getVariance() {
        return variance;
    }

    public MemoryStatisticalSummary setVariance(double variance) {
        this.variance = variance;
        return this;
    }

    @Override
    public double getStandardDeviation() {
        return stdDev;
    }

    public MemoryStatisticalSummary setStdDev(double stdDev) {
        this.stdDev = stdDev;
        return this;
    }

    @Override
    public double getMax() {
        return max;
    }

    public MemoryStatisticalSummary setMax(double max) {
        this.max = max;
        return this;
    }

    @Override
    public double getMin() {
        return min;
    }

    public MemoryStatisticalSummary setMin(double min) {
        this.min = min;
        return this;
    }

    @Override
    public long getN() {
        return count;
    }

    public MemoryStatisticalSummary setN(long count) {
        this.count = count;
        return this;
    }

    @Override
    public double getSum() {
        return sum;
    }

    public MemoryStatisticalSummary setSum(double sum) {
        this.sum = sum;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryStatisticalSummary that = (MemoryStatisticalSummary) o;
        return count == that.count && Double.compare(that.sum, sum) == 0 && Double.compare(that.min, min) == 0 && Double.compare(that.max, max) == 0 && Double.compare(that.mean, mean) == 0 && Double.compare(that.variance, variance) == 0 && Double.compare(that.stdDev, stdDev) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, sum, min, max, mean, variance, stdDev);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MemoryStatisticalSummary.class.getSimpleName() + "[", "]")
                .add("count=" + count)
                .add("sum=" + sum)
                .add("min=" + min)
                .add("max=" + max)
                .add("mean=" + mean)
                .add("variance=" + variance)
                .add("stdDev=" + stdDev)
                .toString();
    }
}
