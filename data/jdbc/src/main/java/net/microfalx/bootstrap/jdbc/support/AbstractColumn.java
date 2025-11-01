package net.microfalx.bootstrap.jdbc.support;

import java.sql.Types;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all column implementations.
 */
public class AbstractColumn<C extends AbstractColumn<C>> extends AbstractSchemaObject<C> implements Column<C> {

    private final Table<?> table;
    private int jdbcType = Types.VARCHAR;
    private boolean nullable;
    private int index;
    private Integer length;

    public AbstractColumn(Table<?> table, String name) {
        super(table.getSchema(), name, Type.COLUMN);
        requireNonNull(table);
        this.table = table;
    }

    @Override
    public final Table<?> getTable() {
        return table;
    }

    @Override
    public final int getJdbcType() {
        return jdbcType;
    }

    public final C withJdbcType(int jdbcType) {
        C copy = copy();
        ((AbstractColumn<C>) copy).jdbcType = jdbcType;
        return copy;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public final C withNullable(boolean nullable) {
        C copy = copy();
        ((AbstractColumn<C>) copy).nullable = nullable;
        return copy;
    }

    @Override
    public final int getIndex() {
        return index;
    }

    @Override
    public final C withIndex(int index) {
        C copy = copy();
        ((AbstractColumn<C>) copy).index = index;
        return copy;
    }

    @Override
    public final Integer getLength() {
        return length;
    }

    @Override
    public C withLength(Integer length) {
        C copy = copy();
        ((AbstractColumn<C>) copy).length = length;
        return copy;
    }
}
