package net.microfalx.bootstrap.model;

/**
 * An exception for expression related errors.
 */
public class ExpressionException extends ModelException {

    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
