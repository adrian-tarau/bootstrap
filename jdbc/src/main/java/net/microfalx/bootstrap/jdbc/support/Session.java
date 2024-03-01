package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * An interfaces which provides an abstraction over a database session.
 */
public interface Session extends Identifiable<String> {

    /**
     * Returns the node hosting this session.
     *
     * @return a non-null instance
     */
    Node getNode();

    /**
     * Returns the schema used by this session.
     *
     * @return a non-empty string
     */
    String getSchema();

    /**
     * Returns the user name owning this session.
     *
     * @return a non-empty string
     */
    String getUserName();

    /**
     * Returns the session state.
     *
     * @return a non-null enum
     */
    State getState();

    /**
     * Returns the statement the session is executing.
     *
     * @return the statement, null if it is executing no statement
     */
    Statement getStatement();

    /**
     * Returns the transaction identifier (the transaction started by this session).
     *
     * @return the transaction identifier, null if a transaction is not associated with the session
     */
    String getTransactionId();

    /**
     * Returns the hostname of the client.
     *
     * @return the hostname
     */
    String getClientHostname();

    /**
     * Returns the time since the session change its state.
     * <ul>
     * <li>the session status is currently ACTIVE, then the value represents the elapsed time in seconds since the session has become active</li>
     * <li>the session status is currently INACTIVE, then the value represents the elapsed time in seconds since the session has become inactive</li>
     * <li>the session status is currently BLOCKED, then the value represents the elapsed time in seconds since the session has become blocked</li>
     * <li>the session status is currently WAITING, then the value represents the elapsed time in seconds since the session has started to wait</li>
     * </ul>
     *
     * @return a positive integer, milliseconds
     */
    Duration getElapsed();

    /**
     * Returns additional information about a session.
     *
     * @return a non-null instance
     */
    String getInfo();

    /**
     * Returns the timestamp when the session was created.
     *
     * @return millis since epoch
     */
    LocalDateTime getCreatedAt();

    /**
     * Returns whether the session belongs to the database.
     *
     * @return <code>true</code> if system, <code>false</code> otherwise
     */
    boolean isSystem();

    /**
     * Cancels the statement executed by this session, if there is any active statement.
     * <p>
     * If the database does not support statement cancellation, the session is closed (which will also cancel any running statement).
     */
    void cancel();

    /**
     * Closes the database sessions.
     * <p>
     * For most database sessions it results in a 'KILL' command.
     */
    void close();

    /**
     * Determines the amount of run-time resources (CPU, I/O bandwidth) the Resource Manager should dedicate for a session.
     */
    enum Priority {

        /**
         * The highest priority, these will receive receive more CPU and I/O resources than those with a MEDIUM or LOW run-time priority
         */
        HIGH,

        /**
         * The default priority
         */
        MEDIUM,

        /**
         * The lowest priority, such sessions will be executed only when resources are not required by HIGH and MEDIUM
         */
        LOW

    }

    enum State {

        /**
         * Session currently executing SQL
         */
        ACTIVE,

        /**
         * Session is idle, waiting for commands
         */
        INACTIVE,

        /**
         * Session is waiting for an event or resource
         */
        WAITING,

        /**
         * Session is blocked by another session (due to a lock)
         */
        BLOCKED,

        /**
         * Session marked to be killed
         */
        KILLED
    }
}
