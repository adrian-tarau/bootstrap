package net.microfalx.bootstrap.model;

/**
 * An exception for type conversion exception.
 */
public class ConversionException extends ModelException {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
