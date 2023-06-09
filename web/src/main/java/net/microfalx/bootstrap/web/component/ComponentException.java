package net.microfalx.bootstrap.web.component;

/**
 * Base class for all {@link Component} exceptions.
 */
public class ComponentException extends RuntimeException {

    public ComponentException(String message) {
        super(message);
    }

    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}
