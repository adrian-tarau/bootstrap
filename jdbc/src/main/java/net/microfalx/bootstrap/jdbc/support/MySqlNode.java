package net.microfalx.bootstrap.jdbc.support;

public class MySqlNode extends AbstractNode {

    public MySqlNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        super(database, id, name, dataSource);
    }
}
