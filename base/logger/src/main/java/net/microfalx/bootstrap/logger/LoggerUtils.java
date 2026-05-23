package net.microfalx.bootstrap.logger;

import ch.qos.logback.classic.LoggerContext;
import net.microfalx.metrics.Metrics;
import org.slf4j.LoggerFactory;

/**
 * Various utilities around loggers.
 */
public class LoggerUtils {

    static Metrics METRICS = Metrics.of("Logger");
    static Metrics METRICS_COUNTS = METRICS.withGroup("Counts");
    static Metrics METRICS_COUNTS_SEVERITY = METRICS_COUNTS.withGroup("Severity");
    static Metrics METRICS_COUNTS_EXCEPTION = METRICS_COUNTS.withGroup("Exception Class");
    static Metrics METRICS_FORWARD_FAILURE = METRICS.withGroup("Forward Failure");
    static Metrics METRICS_FAILURE = METRICS.withGroup("Failure");
    static Metrics METRICS_EVENT_STORE_FAILURE = METRICS.withGroup("Event Failure");
    static Metrics METRICS_ALERT_STORE_FAILURE = METRICS.withGroup("Alert Failure");

    public static final String LOGGER_STORE = "logger";
    public static final String ALERT_STORE = "alert";

    /**
     * Returns the Logback context.
     *
     * @return a non-null instance
     */
    static LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    /**
     * Returns the root logger for Logback.
     *
     * @return a non-null instance
     */
    static ch.qos.logback.classic.Logger getRootLogger() {
        return getLoggerContext().getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    }
}
