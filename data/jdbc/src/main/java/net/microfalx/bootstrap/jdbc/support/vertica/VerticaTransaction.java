package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractTransaction;
import net.microfalx.bootstrap.jdbc.support.Node;

public class VerticaTransaction extends AbstractTransaction {

    public VerticaTransaction(Node node, String id) {
        super(node, id);
    }

    @Override
    public void close() {

    }
}
