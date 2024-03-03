package net.microfalx.bootstrap.jdbc.support;

/**
 * An interface which marks an object statement aware (carries a SQL statement).
 */
public interface StatementAware {

    /**
     * Returns the statement the object is referencing.
     *
     * @return the statement, null if there is no statement
     */
    Statement getStatement();
}
