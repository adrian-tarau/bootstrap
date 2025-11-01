package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all schema object implementations.
 */
public abstract class AbstractSchemaObject<O extends AbstractSchemaObject<O>> implements SchemaObject<O>, Cloneable {

    private final String id;
    private final Schema schema;
    private final String name;
    private final Type type;

    public AbstractSchemaObject(Schema schema, String name, Type type) {
        requireNonNull(schema);
        requireNonNull(name);
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.id = StringUtils.toIdentifier(schema.getId() + "_" + name + "_" + type);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Schema getSchema() {
        return schema;
    }

    @Override
    public final Type getType() {
        return type;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AbstractSchemaObject<?> that)) return false;
        return Objects.equals(schema, that.schema) && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(schema, name);
    }

    @SuppressWarnings("unchecked")
    protected O copy() {
        try {
            return (O) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected final O self() {
        return (O) this;
    }
}
