package net.microfalx.bootstrap.jdbc.migration;

import net.microfalx.bootstrap.jdbc.support.DatabaseException;

/**
 * An exception related to database migration.
 */
public class MigrationException extends DatabaseException {

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
