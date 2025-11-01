package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractDatabase;
import net.microfalx.bootstrap.jdbc.support.AbstractNode;
import net.microfalx.bootstrap.jdbc.support.DataSource;

public class MySqlNode extends AbstractNode {

    public MySqlNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        super(database, id, name, dataSource);
    }
}
