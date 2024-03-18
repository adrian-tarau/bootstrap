package net.microfalx.bootstrap.content;

/**
 * An exception for parsing errors.
 */
public class ContentParsingException extends ContentException {

    public ContentParsingException(String message) {
        super(message);
    }

    public ContentParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
