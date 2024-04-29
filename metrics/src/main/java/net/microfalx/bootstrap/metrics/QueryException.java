package net.microfalx.bootstrap.metrics;

/**
 * An exception raised for an invalid query.
 */
public class QueryException extends MetricException {

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
