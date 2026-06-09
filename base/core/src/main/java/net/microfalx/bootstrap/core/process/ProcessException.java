package net.microfalx.bootstrap.core.process;

/**
 * Base exception for all process executions.
 */
public class ProcessException extends RuntimeException {

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
