package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractView;
import net.microfalx.bootstrap.jdbc.support.Schema;

public class VerticaView extends AbstractView<VerticaView> {

    public VerticaView(Schema schema, String name) {
        super(schema, name);
    }
}
