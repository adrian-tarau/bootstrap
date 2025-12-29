package net.microfalx.bootstrap.jdbc.support;

public abstract class AbstractIndex<I extends AbstractIndex<I>> extends AbstractSchemaObject<I> implements Index<I> {

    private Table<?> table;

    public AbstractIndex(Schema schema, String name) {
        super(schema, name, Type.INDEX);
    }

    @Override
    public final Table<?> getTable() {
        return table;
    }

    @Override
    public boolean exists() {
        return getSchema().getIndexNames().contains(getName());
    }
}
