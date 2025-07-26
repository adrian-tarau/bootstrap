package net.microfalx.bootstrap.model;

/**
 * An exception for data type conversion.
 */
public class InvalidDataTypeExpression extends ModelException {

    public InvalidDataTypeExpression(String message) {
        super(message);
    }

    public InvalidDataTypeExpression(String message, Throwable cause) {
        super(message, cause);
    }
}
