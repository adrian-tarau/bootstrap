package net.microfalx.bootstrap.dataset;

/**
 * An exception used to abort data set operations.
 */
public class DataSetAbortException extends DataSetException {

    public DataSetAbortException(String message) {
        super(message);
    }

    public DataSetAbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
