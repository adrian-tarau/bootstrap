package net.microfalx.bootstrap.cli.command;

/**
 * Base exception for all command failures.
 */
public class CommandException extends RuntimeException{

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
