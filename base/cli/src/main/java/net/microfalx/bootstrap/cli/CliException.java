package net.microfalx.bootstrap.cli;

/**
 * Base exception for all CLI exceptions.
 */
public class CliException extends RuntimeException {

    public CliException(String message) {
        super(message);
    }

    public CliException(String message, Throwable cause) {
        super(message, cause);
    }
}
