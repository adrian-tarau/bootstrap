package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractDatabase;
import net.microfalx.bootstrap.jdbc.support.AbstractNode;
import net.microfalx.bootstrap.jdbc.support.DataSource;

public class VerticaNode extends AbstractNode {

    public VerticaNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        super(database, id, name, dataSource);
    }
}
