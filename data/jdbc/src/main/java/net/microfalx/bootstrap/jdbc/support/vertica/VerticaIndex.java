package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractIndex;
import net.microfalx.bootstrap.jdbc.support.Schema;

public class VerticaIndex extends AbstractIndex<VerticaIndex> {

    public VerticaIndex(Schema schema, String name) {
        super(schema, name);
    }
}
