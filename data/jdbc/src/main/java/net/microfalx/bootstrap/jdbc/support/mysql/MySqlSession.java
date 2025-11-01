package net.microfalx.bootstrap.jdbc.support.mysql;

import net.microfalx.bootstrap.jdbc.support.AbstractSession;
import net.microfalx.bootstrap.jdbc.support.Node;

public class MySqlSession extends AbstractSession {

    private long threadId;

    public MySqlSession(Node node, String id, long threadId) {
        super(node, id);
        this.threadId = threadId;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void close() {

    }
}
