package net.microfalx.bootstrap.model;

/**
 * An exception for data set query parser exceptions.
 */
public class QueryException extends ModelException {

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
