package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;

import java.time.ZonedDateTime;

/**
 * An interfaces which provides an abstraction over a database transaction.
 */
public interface Transaction extends Identifiable<String>, StatementAware {

    /**
     * Returns the node hosting this transaction.
     *
     * @return a non-null instance
     */
    Node getNode();

    /**
     * Returns the state of the transaction.
     *
     * @return a non-null instance
     */
    State getState();

    /**
     * Returns the time when the transaction was started.
     *
     * @return a non-null instance
     */
    ZonedDateTime getStartedAt();

    /**
     * Returns the time when the transaction was started to wait for a lock.
     *
     * @return a non-null instance if {@link #getState()} is {@link State#LOCK_WAIT}, null otherwise
     */
    ZonedDateTime getLockStartedAt();

    /**
     * Returns the transaction weight, based on the number of locked rows and the number of altered rows.
     * To resolve deadlocks, lower weighted transactions are rolled back first. Transactions that have affected
     * non-transactional tables are always treated as having a heavier weight.
     *
     * @return a positive integer if weights are supported, null otherwise
     */
    Integer getWeight();

    /**
     * Returns a state/status about what the transaction is doing.
     *
     * @return the operation, null if not available
     */
    String getOperation();

    /**
     * Returns the number of tables currently being used for processing the current SQL statement.
     *
     * @return a positive integer
     */
    int getTablesInUseCount();

    /**
     * Returns the number of  tables that have row locks held by the current SQL statement.
     *
     * @return a positive integer
     */
    int getTablesLockedCount();

    /**
     * Returns the number of rows the current transaction has locked.
     * <p>
     * THis is an approximation, and may include rows not visible to the current transaction that are
     * delete-marked but physically present.
     *
     * @return a positive integer
     */
    int getLockedRowCount();

    /**
     * Returns the number of rows added or changed in the current transaction.
     *
     * @return a positive integer
     */
    int getModifiedRowCount();

    /**
     * Returns the isolation level of the current transaction.
     *
     * @return a non-null instance
     */
    IsolationLevel getIsolationLevel();

    /**
     * Returns whether the transaction is read-only.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Closes the database transaction.
     * <p>
     * For most database sessions it results in a 'KILL' command.
     */
    void close();

    /**
     * The transaction isolation level
     */
    enum IsolationLevel {

        /**
         * Statements are performed in a non-locking fashion, but a possible earlier version of a row might be used.
         */
        READ_UNCOMMITTED,

        /**
         * Each consistent read, even within the same transaction, sets and reads its own fresh snapshot
         */
        READ_COMMITTED,

        /**
         * All consistent reads within the same transaction read the snapshot established by the first read.
         */
        REPEATABLE_READ,

        /**
         * This level emulates serial transaction execution for all committed transactions
         */
        SERIALIZABLE
    }

    enum State {

        /**
         * Transaction currently running (might execute SQL or just wait idle)
         */
        RUNNING,

        /**
         * Transaction waiting for a lock
         */
        LOCK_WAIT,

        /**
         * Transaction is rolling back.
         */
        ROLLING_BACK,

        /**
         * Transaction is committing.
         */
        COMMITTING
    }
}
