package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractView;
import net.microfalx.bootstrap.jdbc.support.Schema;

public class MySqlView extends AbstractView<MySqlView> {

    public MySqlView(Schema schema, String name) {
        super(schema, name);
    }
}
