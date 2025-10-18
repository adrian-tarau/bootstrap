package net.microfalx.bootstrap.model;

/**
 * An exception thrown when a model is not found.
 */
public class ModelNotFoundException extends ModelException {

    public ModelNotFoundException(String message) {
        super(message);
    }

    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
