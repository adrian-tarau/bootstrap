package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;

import java.util.Set;

/**
 * An interface which represents a database schema.
 */
public interface Schema extends Identifiable<String>, Nameable {

    /**
     * Returns the database this schema belongs to.
     *
     * @return a non-null instance
     */
    Database getDatabase();

    /**
     * Returns the names of all tables in this schema.
     *
     * @return a non-null instance
     */
    Set<String> getTableNames();

    /**
     * Returns the names of all tables in this schema.
     *
     * @return a non-null instance
     */
    Set<String> getIndexNames();

    /**
     * Returns a table by its name.
     *
     * @param name the name of the table
     * @return a non-null instance
     */
    Table<?> getTable(String name);

    /**
     * Returns the names of all indexes in this schema.
     *
     * @return a non-null instance
     */
    Index<?> getIndex(String name);

    /**
     * Returns a database specific query.
     *
     * @param path the relative ( to ~/sql/DATABASE_TYPE/) path
     * @return a non-null instance
     */
    Query getQuery(String path);

    /**
     * Returns a database specific script.
     *
     * @param path the relative ( to ~/sql/DATABASE_TYPE/) path
     * @return a non-null instance
     */
    Script getScript(String path);

    /**
     * Returns a database specific resource.
     *
     * @param path the path relative to sql/DATABASE_TYPE/
     * @return a non-null instance
     */
    Resource getResource(String path);

    /**
     * Returns the JDBC type for the given type name.
     *
     * @param typeName the type name
     * @return a non-null instance
     */
    int getJdbcType(String typeName);

    /**
     * Registers a JDBC type.
     *
     * @param typeName the type name
     * @param jdbcType the JDBC type
     */
    void registerJdbcType(String typeName, int jdbcType);

    /**
     * Clears all cached information.
     */
    void clearCache();
}
