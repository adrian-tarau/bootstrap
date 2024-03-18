package net.microfalx.bootstrap.content;

/**
 * An exception which indicates some content could not be located.
 */
public class ContentNotFoundException extends ContentException {

    public ContentNotFoundException(String message) {
        super(message);
    }

    public ContentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
