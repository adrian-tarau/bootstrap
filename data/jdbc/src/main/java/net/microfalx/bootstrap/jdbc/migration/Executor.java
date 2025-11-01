package net.microfalx.bootstrap.jdbc.migration;

import net.microfalx.bootstrap.jdbc.support.Query;

/**
 * An executor for the database migration
 */
public interface Executor {

    /**
     * Executes a statement.
     *
     * @param query the query
     */
    void execute(Query query);
}
