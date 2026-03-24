package net.microfalx.bootstrap.jdbc.support;

/**
 * A provider for {@link Query}.
 */
public interface QueryProvider {

    /**
     * Creates a query object with a SQL loaded from classpath at directory
     * <code>classpath://sql/TYPE/queries/PATH</code>
     *
     * @param path the relative resource path
     * @return a non-null instance
     */
    Query withResource(String path);

    /**
     * Creates a query object with the provided SQL.
     *
     * @param sql the SQL statement
     * @return a non-null instance
     */
    Query withSql(String sql);
}
