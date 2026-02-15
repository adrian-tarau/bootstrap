package net.microfalx.bootstrap.dos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("bootstrap.dos")
@Setter
@Getter
@ToString
public class DosProperties {

    /**
     * Indicates whether DOS functionality is enabled.
     */
    private boolean enabled = true;

    /**
     * Indicates whether requests from localhost should be tracked.
     */
    private boolean trackLocalhost = false;

    /**
     * How often maintenance tasks should run.
     */
    private Duration maintenanceInterval = ofMinutes(5);

    /**
     * How often to update stats.
     */
    private Duration statsUpdateInterval = ofSeconds(10);

    /**
     * What is the maximum duration of throttling for a single request.
     */
    private Duration maximumThrottlingDuration = ofMinutes(5);

    /**
     * How often rules should be reloaded from the database.
     */
    private Duration reloadInterval = Duration.ofMinutes(15);

    /**
     * Inactivity interval after which tracked requests are removed.
     */
    private Duration inactivityInterval = Duration.ofDays(7);

    /**
     * A threshold applied to any request which is considered a valid request.
     */
    private String accessThreshold = "60 r/s, 60s";

    /**
     * A threshold applied to any request which is considered a failed request (4XX, 5XX, outside security).
     */
    private String failureThreshold = "2 r/s, 5m";

    /**
     * A threshold applied to any request which is considered a failed request (4XX, 5XX, outside security).
     */
    private String notFoundThreshold = "2 r/s, 5m";

    /**
     * A threshold applied to any request which does not passes the business validation.
     */
    private String validationThreshold = "5 r/s, 15m";

    /**
     * A threshold applied to any request which cannot be decoded.
     */
    private String invalidThreshold = "2 r/s, 5m";

    /**
     * A threshold applied to any request which is considered a security violation.
     */
    private String securityThreshold = "5 r/s, 15m";
}
