package net.microfalx.bootstrap.registry;

/**
 * Base exception for all registry failures.
 */
public class RegistryException extends RuntimeException {

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
