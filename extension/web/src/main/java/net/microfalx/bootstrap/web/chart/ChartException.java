package net.microfalx.bootstrap.web.chart;

/**
 * Base exception for all chart exceptions.
 */
public class ChartException extends RuntimeException {

    public ChartException(String message) {
        super(message);
    }

    public ChartException(String message, Throwable cause) {
        super(message, cause);
    }
}
