package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractIndex;
import net.microfalx.bootstrap.jdbc.support.Table;

public class VerticaIndex extends AbstractIndex<VerticaIndex> {

    public VerticaIndex(Table<?> table, String name) {
        super(table, name);
    }
}
