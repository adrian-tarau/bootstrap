package net.microfalx.bootstrap.jdbc.support;

/**
 * A class which represents a database column.
 */
public interface Column<C extends Column<C>> extends SchemaObject<C> {

    /**
     * Returns the table this column belongs to.
     *
     * @return a non-null instance
     */
    Table<?> getTable();

    /**
     * Returns the JDBC type of this column.
     *
     * @return a value from {@code java.sql.Types}
     */
    int getJdbcType();

    /**
     * Creates a new instance with the specified JDBC type.
     *
     * @param jdbcType the JDBC type from {@code java.sql.Types}
     * @return a new instance
     */
    C withJdbcType(int jdbcType);

    /**
     * Returns whether this column allows null values.
     *
     * @return {@code true} if nullable, {@code false} otherwise
     */
    boolean isNullable();

    /**
     * Creates a new instance with the specified nullability.
     *
     * @param nullable {@code true} if nullable, {@code false} otherwise
     * @return a new instance
     */
    C withNullable(boolean nullable);

    /**
     * Returns the position of this column in the table.
     *
     * @return a positive integer
     */
    int getIndex();

    /**
     * Creates a new instance with the specified index.
     *
     * @param index the index
     * @return a new instance
     */
    C withIndex(int index);

    /**
     * Returns the length of this column (VARCHAR).
     *
     * @return a positive integer or {@code null} if not applicable
     */
    Integer getLength();

    /**
     * Creates a new instance with the specified length.
     *
     * @param length the new length
     * @return a new instance
     */
    C withLength(Integer length);

}
