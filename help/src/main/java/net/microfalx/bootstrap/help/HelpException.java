package net.microfalx.bootstrap.help;

/**
 * An exception for help errors.
 */
public class HelpException extends RuntimeException {

    public HelpException(String message) {
        super(message);
    }

    public HelpException(String message, Throwable cause) {
        super(message, cause);
    }
}
