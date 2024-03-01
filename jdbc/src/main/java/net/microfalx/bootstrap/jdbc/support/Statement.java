package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;

/**
 * A class which represents a SQL (statement).
 * <p>
 * Clients can asks for metdata about the SQL by executing a soft-parsing on the client side.
 * The clients should not expect to receive a correct representation of the compiled statement. While the library supports a wide range of queries,
 * not everything can be extracted correctly.
 */
public interface Statement extends Identifiable<String> {

    /**
     * Creates a statement instance.
     *
     * @param content the SQL content
     * @return a non-null instance
     */
    static Statement create(String content) {
        return new StatementImpl(content);
    }

    /**
     * Returns the type of statement.
     *
     * @return a non-null instance
     */
    Type getType();

    /**
     * Returns the SQL for this statement.
     *
     * @return a non-null
     */
    String getContent();

    /**
     * Type of script.
     */
    enum Type {

        /**
         * An unknown type
         */
        UNKNOWN,

        /**
         * Creates objects in the database.
         */
        CREATE,

        /**
         * Alters the structure of the database.
         */
        ALTER,

        /**
         * Deletes objects from the database.
         */
        DROP,

        /**
         * Removes all records from a table, including all spaces allocated for the records are removed.
         */
        TRUNCATE,

        /**
         * Renames an object.
         */
        RENAME,

        /**
         * Retrieves data from the a database.
         */
        SELECT,

        /**
         * Inserts data into a table or more.
         */
        INSERT,

        /**
         * Deletes all records from a table, the space for the records remains.
         */
        DELETE,

        /**
         * Updates existing data within a table.
         */
        UPDATE,

        /**
         * UPSERT operation (insert or update).
         */
        MERGE,

        /**
         * Calls a procedure/function.
         */
        CALL,

        /**
         * Loads data into the database
         */
        LOAD,

        /**
         * Set a connection attribute.
         */
        SET,

        /**
         * optimizes data store, moves data around.
         */
        OPTIMIZE,

        /**
         * Controls concurrency.
         */
        LOCK
    }
}
