package net.microfalx.bootstrap.metrics;

/**
 * Base class for all metric exceptions.
 */
public class MetricException extends RuntimeException {

    public MetricException(String message) {
        super(message);
    }

    public MetricException(String message, Throwable cause) {
        super(message, cause);
    }
}
