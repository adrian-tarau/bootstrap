package net.microfalx.bootstrap.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;

@Configuration
@ConfigurationProperties("bootstrap.retry")
public class RetryProperties {

    private int maxAttempts = 3;

    /**
     * The default 'initialInterval' value - 100 millisecs. Coupled with the default
     * 'multiplier' value this gives a useful initial spread of pauses for 1-5 retries.
     */
    private long initialInterval = ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL;

    /**
     * The maximum value of the backoff period in milliseconds.
     */
    private long maxInterval = ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL;

    /**
     * The value to add to the backoff period for each retry attempt.
     */
    private double multiplier = ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;


    public long getInitialInterval() {
        return initialInterval;
    }

    public void setInitialInterval(long initialInterval) {
        this.initialInterval = initialInterval;
    }

    public long getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
