package net.microfalx.bootstrap.logger;

import net.microfalx.metrics.Metrics;

/**
 * Various utilities around loggers.
 */
public class LoggerUtils {

    static Metrics METRICS = Metrics.of("Logger");
    static Metrics METRICS_COUNTS = METRICS.withGroup("Counts");
    static Metrics METRICS_COUNTS_SEVERITY = METRICS_COUNTS.withGroup("Severity");
    static Metrics METRICS_COUNTS_EXCEPTION = METRICS_COUNTS.withGroup("Exception Class");
    static Metrics METRICS_FORWARD_FAILURE = METRICS.withGroup("Forward Failure");
    static Metrics METRICS_EVENT_STORE_FAILURE = METRICS.withGroup("Event Failure");
    static Metrics METRICS_ALERT_STORE_FAILURE = METRICS.withGroup("Alert Failure");

    public static final String LOGGER_STORE = "logger";
    public static final String ALERT_STORE = "alert";
}
