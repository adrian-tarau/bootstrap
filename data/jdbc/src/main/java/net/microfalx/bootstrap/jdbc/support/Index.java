package net.microfalx.bootstrap.jdbc.support;

/**
 * An interface which represents a database index.
 */
public interface Index<I extends Index<I>> extends SchemaObject<I> {

    /**
     * Returns the table this index belongs to.
     *
     * @return a non-null instance
     */
    Table<?> getTable();
}
