package net.microfalx.bootstrap.help;

/**
 * An exception thrown when a Table of Contents (ToC) is not found.
 */
public class TocNotFoundException extends HelpException {

    public TocNotFoundException(String message) {
        super(message);
    }

    public TocNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
