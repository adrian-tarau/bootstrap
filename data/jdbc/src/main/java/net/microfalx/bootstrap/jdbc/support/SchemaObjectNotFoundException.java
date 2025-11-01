package net.microfalx.bootstrap.jdbc.support;

/**
 * Thrown when a schema object is not found.
 */
public class SchemaObjectNotFoundException extends DatabaseException {

    public SchemaObjectNotFoundException(String message) {
        super(message);
    }
}
