package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.EnumUtils.fromName;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.TimeUtils.toLocalDateTime;

public class VerticaDatabase extends AbstractDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticaDatabase.class);

    private volatile Collection<Node> cachedNodes = Collections.emptyList();

    public VerticaDatabase(DatabaseService databaseService, String id, String name, DataSource dataSource) {
        super(databaseService, id, name, dataSource);
    }

    @Override
    public Type getType() {
        return Type.VERTICA;
    }

    @Override
    protected Collection<Node> extractNodes() throws SQLException {
        JdbcTemplate template = new JdbcTemplate(getDataSource().unwrap());
        cachedNodes = template.query(GET_NODES_SQL, rs -> {
            Collection<Node> nodes = new ArrayList<>();
            while (rs.next()) {
                String id = rs.getString("node_id");
                String name = rs.getString("node_name");
                String state = rs.getString("node_state");
                String host = rs.getString("export_address");
                Node.State stateEnum = fromName(Node.State.class, state, Node.State.UNKNOWN);
                int port = getPortFromHostAndPort(host);
                host = getHostFromHostAndPort(host);
                VerticaNode node = createNode(id, name, host, port);
                node.setState(stateEnum);
                nodes.add(node);
            }
            return nodes;
        });
        return cachedNodes;
    }

    @Override
    protected Collection<Session> extractSessions() throws SQLException {
        JdbcTemplate template = new JdbcTemplate(getDataSource().unwrap());
        return template.query(GET_SESSIONS_SQL, rs -> {
            Collection<Session> sessions = new ArrayList<>();
            while (rs.next()) {
                String sessionId = rs.getString("session_id");
                String nodeName = rs.getString("node_name");
                Node node = getNodeByName(nodeName);
                if (node == null) {
                    LOGGER.error("Cannot find a node with name " + nodeName);
                    continue;
                }
                VerticaSession session = new VerticaSession(node, sessionId);
                session.setUserName(rs.getString("user_name"));
                session.setSchema(session.getUserName());
                session.setClientHostname(getHostFromHostAndPort(rs.getString("client_hostname")));
                session.setState(getState(rs));
                Timestamp statementStart = rs.getTimestamp("statement_start");
                if (statementStart != null) {
                    session.setStartedAt(TimeUtils.toLocalDateTime(statementStart));
                }
                String currentStatement = rs.getString("current_statement");
                String lastStatement = rs.getString("last_statement");
                if (session.getState() == Session.State.ACTIVE && StringUtils.isEmpty(currentStatement)) {
                    currentStatement = lastStatement;
                }
                if (isNotEmpty(currentStatement)) session.setStatement(Statement.create(currentStatement));
                session.setCreated(toLocalDateTime(rs.getTimestamp("login_timestamp")));
                sessions.add(session);
            }
            return sessions;
        });
    }

    @Override
    protected Collection<Transaction> extractTransactions() throws SQLException {
        return Collections.emptyList();
    }

    private VerticaNode createNode(String id, String name, DataSource dataSource) {
        return new VerticaNode(this, id, name, dataSource);
    }

    private VerticaNode createNode(String id, String name, String hostName, int port) {
        DataSource dataSource = createDataSource(hostName, port);
        return createNode(id, name, dataSource);
    }

    @Override
    public Collection<Transaction> getTransactions() {
        return Collections.emptyList();
    }

    private Session.State getState(ResultSet rs) throws SQLException {
        String transactionId = rs.getString("transaction_id");
        boolean hasValidTransactionId = isNotEmpty(transactionId) && !"-1".equals(transactionId) && !"0".equals(transactionId);
        return hasValidTransactionId ? Session.State.ACTIVE : Session.State.INACTIVE;
    }

    private Node getNodeByName(String nodeName) {
        Collection<Node> nodes = cachedNodes.isEmpty() ? getNodes() : cachedNodes;
        return cachedNodes.stream().filter(node -> node.getName().equals(nodeName)).findFirst().orElse(null);
    }

    private static final String GET_NODES_SQL = "select * from v_catalog.nodes order by node_name";
    private static final String GET_SESSIONS_SQL = "select * from v_monitor.sessions";
    private static final String GET_SESSION_STATES_SQL = "select sum(CASE WHEN transaction_id::integer > 0 THEN 1 ELSE 0 END) as active, \n" +
            "        sum(CASE WHEN transaction_id::integer <= 0 THEN 1 ELSE 0 END) as inactive, \n" +
            "        sum(CASE WHEN lock_mode is not null THEN 1 ELSE 0 END) as blocked from (\n" +
            "        select s.transaction_id, l.lock_mode from v_monitor.sessions s left join v_monitor.locks l on s.transaction_id = l.transaction_id\n" +
            ") as t";
    private static final String GET_RESOURCE_QUEUES = "select * from v_monitor.resource_queues";
}
