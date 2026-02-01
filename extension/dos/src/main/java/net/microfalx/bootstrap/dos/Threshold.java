package net.microfalx.bootstrap.dos;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.TimeUtils;

import java.time.Duration;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FormatterUtils.formatDuration;

/**
 * Holds DoS thresholds.
 */
@ToString
@Slf4j
public class Threshold {

    public static final Threshold DEFAULT_THRESHOLD = Threshold.create(1, "15m");

    private final float requestRate;
    private final Duration blockingPeriod;

    /**
     * Creates a threshold from the string representation of the threshold parameters.
     *
     * @param requestRate    the request rate
     * @param blockingPeriod the blocking period
     * @return a non-null instance
     */
    public static Threshold create(float requestRate, String blockingPeriod) {
        return create(requestRate, TimeUtils.parseDuration(blockingPeriod));
    }

    /**
     * Creates a threshold.
     *
     * @param requestRate    the request rate
     * @param blockingPeriod the blocking period
     * @return a non-null instance
     */
    public static Threshold create(float requestRate, Duration blockingPeriod) {
        return new Threshold(requestRate, blockingPeriod);
    }

    private Threshold(float requestRate, Duration blockingPeriod) {
        requireNotEmpty(requestRate);
        requireNonNull(blockingPeriod);
        this.requestRate = requestRate;
        this.blockingPeriod = blockingPeriod;
    }

    /**
     * Returns the maximum request rate allowed from an IP address.
     *
     * @return a positive number, in requests per second
     */
    public float getRequestRate() {
        return requestRate;
    }

    /**
     * Returns the amount of time that a client (IP address) will be blocked for if they are added to the blocked list.
     * <p>
     * During this time, all subsequent requests from the client will result in a 403 (Forbidden) error and the timer being reset (defaults to 10 seconds).
     *
     * @return a non-null instance
     */
    public Duration getBlockingPeriod() {
        return blockingPeriod;
    }

    /**
     * Returns a description of the threshold.
     *
     * @return a non-null instance
     */
    public String toDescription() {
        return "request rate=" + requestRate + " r/s, blocking period=" + formatDuration(blockingPeriod);
    }

}
