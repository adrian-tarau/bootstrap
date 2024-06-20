package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * An interfaces which provides an abstraction over a database node.
 */
public interface Node extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Returns the display name for a node.
     *
     * @return a non-null instance
     */
    String getDisplayName();

    /**
     * Returns the time zone of the node/database.
     *
     * @return a non-null instance
     */
    ZoneId getZoneId();

    /**
     * Returns the state of the node.
     *
     * @return a non-null instance
     */
    State getState();

    /**
     * Returns the data source which provides connectivity to this node.
     *
     * @return a non-null instance
     */
    DataSource getDataSource();

    /**
     * Returns the database which owns this node.
     *
     * @return a non-null instance
     */
    Database getDatabase();

    /**
     * Returns the hostname of the node.
     *
     * @return a non-null instance
     */
    String getHostname();

    /**
     * Returns the port of the service running at the {@link #getHostname()}.
     *
     * @return the port
     */
    int getPort();

    /**
     * lReturns the timestamp when the node was started.
     *
     * @return a non-null instance
     */
    LocalDateTime getStartedAt();

    /**
     * Returns the timestamp time when the node was created.
     *
     * @return a non-null instance
     */
    LocalDateTime getCreatedAt();

    /**
     * Returns the timestamp when the node was changed last time.
     *
     * @return a non-null instance
     */
    LocalDateTime getModifiedAt();

    /**
     * Returns whether the node is available for use (to be queries for independent node).
     *
     * @return {@code true} if available, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Validates the data source parameters node accessibility.
     */
    void validate();

    /**
     * Returns the error message received during last validation.
     *
     * @return null if the node is available, a non-null string with the reason why validation failed.
     */
    String getValidationError();

    /**
     * An enum for node states.
     */
    enum State {

        /**
         * An unknown state.
         */
        UNKNOWN(false, false),

        /**
         * Node is up & running, healthy.
         */
        UP(true, true),

        /**
         * Node is down & inaccessible.
         */
        DOWN(false, false),

        /**
         * Node is up & running, but inaccessible for queries since it is in recovery (restoring for a backup, synchronizing data from a different node, etc).
         */
        RECOVERING(true, false),

        /**
         * Node is up & running, but it is a stand-by node (not used for queries).
         */
        STANDBY(true, false);

        boolean available;
        boolean queryable;

        State(boolean available, boolean queryable) {
            this.available = available;
            this.queryable = queryable;
        }

        /**
         * Returns whether the node state indicates a node which is up, running and in a healthy state (available to us),
         * <p>
         * Some databases have nodes which are up, running but they are not participating in the cluster. There are some nodes which are recovering aftr downtime (and they will
         * not recevie requests - like {@link #RECOVERING}, or they are up and running ready to take over - like {@link #STANDBY}
         * <p>
         * A node which says is "unavailable" should be reported as a problem
         *
         * @return <code>true</code> if available, <code>false</code> otherwise
         */
        public boolean isAvailable() {
            return available;
        }

        /**
         * Returns whether the node state indicates that the node is up, running and can receive queries.
         *
         * @return <code>true</code> if cna be queried, <code>false</code> otherwise
         */
        public boolean isQueryable() {
            return queryable;
        }
    }
}
