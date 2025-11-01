package net.microfalx.bootstrap.jdbc.support;

public abstract class AbstractIndex<I extends AbstractIndex<I>> extends AbstractSchemaObject<I> implements Index<I> {

    private final Table<?> table;

    public AbstractIndex(Table<?> table, String name) {
        super(table.getSchema(), name, Type.INDEX);
        this.table = table;
    }

    @Override
    public final Table<?> getTable() {
        return table;
    }
}
