package net.microfalx.bootstrap.jdbc.support;

public class VerticaNode extends AbstractNode {

    public VerticaNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        super(database, id, name, dataSource);
    }
}
