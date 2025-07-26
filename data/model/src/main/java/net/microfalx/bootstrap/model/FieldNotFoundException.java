package net.microfalx.bootstrap.model;

/**
 * An exception for a missing field.
 */
public class FieldNotFoundException extends ModelException {

    public FieldNotFoundException(String message) {
        super(message);
    }

    public FieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
