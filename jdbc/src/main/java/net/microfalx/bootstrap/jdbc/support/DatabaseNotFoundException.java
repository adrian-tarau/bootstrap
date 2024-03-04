package net.microfalx.bootstrap.jdbc.support;

public class DatabaseNotFoundException extends DatabaseException{

    public DatabaseNotFoundException(String message) {
        super(message);
    }

    public DatabaseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
