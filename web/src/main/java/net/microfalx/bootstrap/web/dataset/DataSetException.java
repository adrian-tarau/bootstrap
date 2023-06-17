package net.microfalx.bootstrap.web.dataset;

/**
 * Base class for all data set exceptions.
 */
public class DataSetException extends RuntimeException {

    public DataSetException(String message) {
        super(message);
    }

    public DataSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
