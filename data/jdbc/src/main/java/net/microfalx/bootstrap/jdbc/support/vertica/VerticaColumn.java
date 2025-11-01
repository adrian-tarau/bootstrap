package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractColumn;
import net.microfalx.bootstrap.jdbc.support.Table;

public class VerticaColumn extends AbstractColumn<VerticaColumn> {

    public VerticaColumn(Table<?> table, String name) {
        super(table, name);
    }
}
