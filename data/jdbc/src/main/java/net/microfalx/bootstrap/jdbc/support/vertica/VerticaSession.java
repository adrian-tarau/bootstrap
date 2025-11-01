package net.microfalx.bootstrap.jdbc.support.vertica;

import net.microfalx.bootstrap.jdbc.support.AbstractSession;
import net.microfalx.bootstrap.jdbc.support.Node;
import org.springframework.jdbc.core.JdbcTemplate;

public class VerticaSession extends AbstractSession {

    public VerticaSession(Node node, String id) {
        super(node, id);
    }

    @Override
    public void cancel() {

    }

    @Override
    public void close() {
        JdbcTemplate template = new JdbcTemplate(getNode().getDataSource().unwrap());
        template.execute("SELECT close_session ('" + getId() + "');");
    }
}
