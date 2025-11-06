package net.microfalx.bootstrap.jdbc.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all table implementations.
 */
public abstract class AbstractTable<T extends AbstractTable<T>> extends AbstractSchemaObject<T> implements Table<T> {

    private volatile Columns columns;
    private volatile Indexes indexes;

    public AbstractTable(Schema schema, String name) {
        super(schema, name, Type.TABLE);
    }

    @Override
    public List<Column<?>> getColumns() {
        checkIfColumnsLoaded();
        return unmodifiableList(columns.columnsByIndex);
    }

    @Override
    public Column<?> findColumn(String name) {
        requireNonNull(name);
        checkIfColumnsLoaded();
        return columns.columnByName.get(name.toLowerCase());
    }

    @Override
    public Column<?> getColumn(String name) {
        checkIfColumnsLoaded();
        Column<?> column = findColumn(name);
        if (column == null) {
            throw new SchemaObjectNotFoundException("Column with name " + name + " not found in table " + getName());
        }
        return column;
    }

    @Override
    public boolean exists() {
        return getSchema().getTableNames().contains(getName());
    }

    protected abstract Columns loadColumns();

    protected abstract Indexes loadIndexes();

    private void checkIfColumnsLoaded() {
        if (columns == null) columns = loadColumns();
    }

    private void checkIfIndexesLoaded() {
        if (indexes == null) indexes = loadIndexes();
    }

    public static class Columns {

        private final List<Column<?>> columnsByIndex = new ArrayList<>();
        private final Map<String, Column<?>> columnByName = new HashMap<>();

        public void addColumn(Column<?> column) {
            requireNonNull(column);
            columnsByIndex.add(column);
            columnByName.put(column.getName().toLowerCase(), column);
        }
    }

    protected static class Indexes {

        private final List<Index<?>> indexByIndex = new ArrayList<>();
        private final Map<String, Index<?>> indexByName = new HashMap<>();

        public void addColumn(Index<?> index) {
            requireNonNull(index);
            indexByIndex.add(index);
            indexByName.put(index.getName().toLowerCase(), index);
        }
    }
}
