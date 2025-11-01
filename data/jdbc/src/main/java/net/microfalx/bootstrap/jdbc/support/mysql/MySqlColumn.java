package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractColumn;
import net.microfalx.bootstrap.jdbc.support.Table;

public class MySqlColumn extends AbstractColumn<MySqlColumn> {

    public MySqlColumn(Table<?> table, String name) {
        super(table, name);
    }
}
