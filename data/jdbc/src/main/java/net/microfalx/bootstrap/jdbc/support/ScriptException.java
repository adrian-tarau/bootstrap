package net.microfalx.bootstrap.jdbc.support;

/**
 * An exception related to script processing.
 */
public class ScriptException extends DatabaseException {

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
