package net.microfalx.bootstrap.dataset;

/**
 * An exception raised when an operation fails due to constraints violation.
 */
public class DataSetConstraintViolationException extends DataSetException {

    public DataSetConstraintViolationException(String message) {
        super(message);
    }

    public DataSetConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
