package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.time.ZonedDateTime;

/**
 * A class which represents a SQL (statement).
 * <p>
 * Clients can ask for metadata about the SQL by executing a soft-parsing on the client side.
 * The clients should not expect to receive a correct representation of the compiled statement. While the library supports a wide range of queries,
 * not everything can be extracted correctly.
 */
public interface Statement extends Identifiable<String> {

    /**
     * Creates a statement instance.
     * <p>
     * The statement will be assigned to an {@code anonymous} user name. Use {@link #withUserName(String)} to change
     * the user name which triggered the statement.
     *
     * @param node    the node running the SQL statement
     * @param content the SQL content
     * @return a non-null instance
     */
    static Statement create(Node node, String content) {
        return new StatementImpl(node, content, null);
    }

    /**
     * Creates a statement instance.
     *
     * @param node     the node running the SQL statement
     * @param content  the SQL content
     * @param userName the user name which triggered the SQL
     * @return a non-null instance
     */
    static Statement create(Node node, String content, String userName) {
        return new StatementImpl(node, content, userName);
    }

    /**
     * Returns the node which was running the statement.
     *
     * @return a non-null instance
     */
    Node getNode();

    /**
     * Returns the user name which was executing the statement.
     *
     * @return a non-null instance
     */
    String getUserName();

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
     * Returns the timestamp when the statement was executed.
     * @return a non-null instance
     */
    ZonedDateTime getExecutionTime();

    /**
     * Returns statistics about a statement.
     *
     * @return a non-null instance
     */
    Statistics getStatistics();

    /**
     * Creates a copy of the statement and attaches new user.
     *
     * @param userName the new user-name
     * @return a new instance
     */
    Statement withUserName(String userName);

    /**
     * Creates a copy of the statement and changes the execution time.
     * @param executionTime the new execution time
     * @return a new instance
     */
    Statement withExecutionTime(ZonedDateTime executionTime);

    /**
     * Creates a copy of the statement and attaches new statistics.
     *
     * @param statistics the new statistics
     * @return a new instance
     */
    Statement withStatistics(Statistics statistics);

    /**
     * Creates a copy of the statement and attaches new statistics.
     *
     * @param statisticalSummary the new statistics
     * @return a new instance
     */
    Statement withStatistics(StatisticalSummary statisticalSummary);

    /**
     * An interface which holds statistics about a statement.
     */
    interface Statistics extends StatisticalSummary {

    }

    /**
     * Type of script.
     */
    enum Type {

        /**
         * An unknown type
         */
        OTHER,

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
