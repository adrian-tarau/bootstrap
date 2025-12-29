package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractIndex;
import net.microfalx.bootstrap.jdbc.support.Schema;

public class MySqlIndex extends AbstractIndex<MySqlIndex> {

    public MySqlIndex(Schema schema, String name) {
        super(schema, name);
    }
}
