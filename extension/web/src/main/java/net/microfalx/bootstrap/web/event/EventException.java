package net.microfalx.bootstrap.web.event;

/**
 * Base exception for all event related exceptions.
 */
public class EventException extends RuntimeException {

    public EventException(String message) {
        super(message);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}
