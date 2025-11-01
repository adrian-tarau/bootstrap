package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

/**
 * A common interface for schema objects (tables, views, etc).
 */
public interface SchemaObject<O extends SchemaObject<O>> extends Identifiable<String>, Nameable {

    /**
     * Returns the type of this object.
     *
     * @return a non-null instance
     */
    Type getType();

    /**
     * Returns the schema this object belongs to.
     *
     * @return a non-null instance
     */
    Schema getSchema();

    /**
     * Returns whether the object exists in the schema.
     *
     * @return {@code true} if the object exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * An enumeration representing schema object types.
     */
    enum Type {
        TABLE,
        VIEW,
        COLUMN,
        INDEX,
        OTHER
    }
}
