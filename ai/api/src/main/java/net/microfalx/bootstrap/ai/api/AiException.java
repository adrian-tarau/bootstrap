package net.microfalx.bootstrap.ai.api;

/**
 * Base class for all exceptions thrown by the AI framework.
 */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
