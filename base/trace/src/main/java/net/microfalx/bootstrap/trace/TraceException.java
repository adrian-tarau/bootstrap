package net.microfalx.bootstrap.trace;

/**
 * Base class for all tracing exceptions.
 */
public class TraceException extends RuntimeException {

    public TraceException(String message) {
        super(message);
    }

    public TraceException(String message, Throwable cause) {
        super(message, cause);
    }
}
