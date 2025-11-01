package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractIndex;
import net.microfalx.bootstrap.jdbc.support.Table;

public class MySqlIndex extends AbstractIndex<MySqlIndex> {

    public MySqlIndex(Table<?> table, String name) {
        super(table, name);
    }
}
