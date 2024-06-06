package net.microfalx.bootstrap.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;

@Configuration
@ConfigurationProperties("bootstrap.retry")
@Getter
@Setter
@ToString
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

}
