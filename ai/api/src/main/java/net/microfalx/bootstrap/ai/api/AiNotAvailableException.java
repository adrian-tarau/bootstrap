package net.microfalx.bootstrap.ai.api;

/**
 * An exception for an AI interface being not available for use.
 */
public class AiNotAvailableException extends AiException {

    public AiNotAvailableException(String message) {
        super(message);
    }

    public AiNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
