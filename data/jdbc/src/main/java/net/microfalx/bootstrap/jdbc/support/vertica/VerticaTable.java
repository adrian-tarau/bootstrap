package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractTable;
import net.microfalx.bootstrap.jdbc.support.Schema;

public class VerticaTable extends AbstractTable<VerticaTable> {

    public VerticaTable(Schema schema, String name) {
        super(schema, name);
    }

    @Override
    protected Columns loadColumns() {
        return null;
    }

    @Override
    protected Indexes loadIndexes() {
        return null;
    }
}
