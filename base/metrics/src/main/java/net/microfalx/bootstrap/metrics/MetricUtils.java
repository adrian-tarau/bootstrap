package net.microfalx.bootstrap.metrics;

import net.microfalx.lang.IdGenerator;

import java.time.Duration;

/**
 * Various utilities around metrics.
 */
public class MetricUtils {

    /**
     * Returns the id generator for metrics objects.
     *
     * @return a non-null instance
     */
    static IdGenerator getIdGenerator() {
        return IdGenerator.get("metrics");
    }

    /**
     * Returns the next identifier for metrics objects.
     *
     * @param prefix the prefix
     * @return a non-null string
     */
    static String nextId(String prefix) {
        return prefix + "_" + getIdGenerator().nextAsString();
    }

    /**
     * Rounds the duration at 5s, 60s or 5min, depending on the value.
     *
     * @param duration the original duration
     * @return the rounded duration
     */
    public static Duration round(Duration duration) {
        long seconds = duration.toSeconds();
        if (seconds < 60) {
            seconds = (seconds / 5) * 5;
        } else if (seconds < 300) {
            seconds = (seconds / 60) * 60;
        } else {
            seconds = (seconds / 300) * 300;
        }
        return Duration.ofSeconds(seconds);
    }
}
