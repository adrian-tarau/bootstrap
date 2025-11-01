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
     * @return the column instance
     */
    Column<?> getColumn(String name);

}
