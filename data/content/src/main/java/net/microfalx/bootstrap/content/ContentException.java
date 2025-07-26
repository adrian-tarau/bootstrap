package net.microfalx.bootstrap.content;

/**
 * Base class for all content exceptions.
 */
public class ContentException extends RuntimeException{

    public ContentException(String message) {
        super(message);
    }

    public ContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
