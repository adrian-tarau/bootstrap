package net.microfalx.bootstrap.search;

/**
 * An exception for index engine failures.
 */
public class IndexException extends RuntimeException {

    public IndexException(String message) {
        super(message);
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }
}
