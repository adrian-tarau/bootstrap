package net.microfalx.bootstrap.jdbc.support;

import java.util.List;

/**
 * An interface which represents a database table.
 */
public interface Table<T extends Table<T>> extends SchemaObject<T> {

    /**
     * Returns the columns of this table.
     *
     * @return a non-null instance
     */
    List<Column<?>> getColumns();

    /**
     * Returns a column by its name.
     *
     * @param name the column name
     * @return the column instance, null if it does not exist
     */
    Column<?> findColumn(String name);

    /**
     * Returns a column by its name.
     *
     * @param name the column name
     * @return the column instance
     * @throws SchemaObjectNotFoundException if the column does not exist
     */
    Column<?> getColumn(String name);

}
