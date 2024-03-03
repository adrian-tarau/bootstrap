package net.microfalx.bootstrap.jdbc.support;

public class VerticaTransaction extends AbstractTransaction {

    public VerticaTransaction(Node node, String id) {
        super(node, id);
    }

    @Override
    public void close() {

    }
}
