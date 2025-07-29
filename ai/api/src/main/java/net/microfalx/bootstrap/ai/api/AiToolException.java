package net.microfalx.bootstrap.ai.api;

/**
 * An exception that indicates an error occurred while executing a tool.
 */
public class AiToolException extends AiException {

    public AiToolException(String message) {
        super(message);
    }

    public AiToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
