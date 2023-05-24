package net.microfalx.bootstrap.search;

/**
 * An exception for search engine failures.
 */
public class SearchException extends RuntimeException {

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
