package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractTransaction;
import net.microfalx.bootstrap.jdbc.support.Node;

public class MySqlTransaction extends AbstractTransaction {

    private long transactionId;
    private long threadId;

    public MySqlTransaction(Node node, String id, long transactionId, long threadId) {
        super(node, id);
        this.transactionId = transactionId;
        this.threadId = threadId;
    }

    @Override
    public void close() {

    }
}
