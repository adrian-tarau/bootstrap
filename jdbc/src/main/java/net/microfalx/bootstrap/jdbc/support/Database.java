package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.annotation.Name;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * An interfaces which provides an abstraction over a database.
 */
public interface Database extends Node {

    /**
     * Returns the type of database (vendor).
     *
     * @return a non-null instance
     */
    Type getType();

    /**
     * Returns the nodes supporting this database.
     * <p>
     * If the database has a single node, it returns the database.
     *
     * @return a non-null instance
     */
    Collection<Node> getNodes();

    /**
     * Returns a node by its identifier.
     *
     * @param id the node identifier
     * @return a non-null optional
     */
    Optional<Node> getNode(String id);

    /**
     * Returns the sessions for this database.
     *
     * @return a non-null instance
     */
    Collection<Session> getSessions();

    /**
     * Returns the transactions for this database.
     *
     * @return a non-null instance
     */
    Collection<Transaction> getTransactions();

    /**
     * Returns the statements executed between a given interval.
     * <p>
     * Not all databases can extract the execution history of statements and even when possible, it might not have
     * any statistics attached to the statement.
     * <p>
     * The statement is expected to
     *
     * @param start the start time
     * @param end   the end time
     * @return a non-null instance
     */
    Collection<Statement> getStatements(LocalDateTime start, LocalDateTime end);

    /**
     * An enum which identifies the database type.
     */
    enum Type {

        /**
         * A <a href="https://dev.mysql.com/doc/refman/8.3/en/">MySQL</a> database.
         */
        @Name("MySQL")
        MYSQL,

        /**
         * A <a href="https://mariadb.org/documentation/">MariaDB</a> database.
         */
        @Name("MariaDB")
        MARIADB,

        /**
         * A <a href="https://www.postgresql.org/docs/current/index.html">PostgreSQL</a> database.
         */
        @Name("PostgreSQL")
        POSTGRES,

        /**
         * A <a href="https://docs.vertica.com/24.1.x/en/">Vertica</a> database.
         */
        @Name("Vertica")
        VERTICA
    }
}
