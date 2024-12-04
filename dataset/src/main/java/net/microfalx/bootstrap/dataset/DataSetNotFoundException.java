package net.microfalx.bootstrap.dataset;

/**
 * An exception used to raise issues about missing records or fields.
 */
public class DataSetNotFoundException extends DataSetException {

    public DataSetNotFoundException(String message) {
        super(message);
    }

    public DataSetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
