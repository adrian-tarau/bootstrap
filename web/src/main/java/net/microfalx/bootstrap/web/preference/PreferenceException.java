package net.microfalx.bootstrap.web.preference;

/**
 * Base exception for all preference exceptions.
 */
public class PreferenceException extends RuntimeException {

    public PreferenceException(String message) {
        super(message);
    }

    public PreferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
