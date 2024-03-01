package net.microfalx.bootstrap.jdbc.support;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

import static java.util.Collections.singleton;
import static net.microfalx.lang.StringUtils.*;

public class MySqlDatabase extends AbstractDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final int TABLE_NOT_FOUND_ERROR = 1146;
    private static final String GARB_NODE_NAME = "garb";

    private volatile boolean clustered = true;

    public MySqlDatabase(DatabaseService databaseService, String id, String name, DataSource dataSource) {
        super(databaseService, id, name, dataSource);
    }

    @Override
    public Type getType() {
        return Type.MYSQL;
    }

    @Override
    public Collection<Transaction> getTransactions() {
        return Collections.emptyList();
    }

    @Override
    protected Collection<Node> extractNodes() throws SQLException {
        if (clustered) {
            try {
                return extractNodesFromCluster();
            } catch (DataAccessException e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof SQLException sqlException) {
                    int errorCode = sqlException.getErrorCode();
                    clustered = !(errorCode == TABLE_NOT_FOUND_ERROR);
                    if (clustered) LOGGER.error("Could not extract nodes from " + getName(), e);
                } else {
                    return net.microfalx.lang.ExceptionUtils.throwException(e);
                }
            }
        }
        return singleton(createNode(getId(), getName(), getDataSource()));
    }

    @Override
    protected Collection<Session> extractSessions() throws SQLException {
        Collection<Session> sessions = new ArrayList<>();
        Collection<Node> nodes = getNodes();
        for (Node node : nodes) {
            sessions.addAll(time("Extract Sessions - " + node.getName(), () -> extractSessionsFromNode(node)));
        }
        return sessions;
    }

    @Override
    protected Collection<Transaction> extractTransactions() throws SQLException {
        return Collections.emptyList();
    }

    private MySqlNode createNode(String id, String name, DataSource dataSource) {
        return new MySqlNode(this, id, name, dataSource);
    }

    private MySqlNode createNode(String id, String name, String hostName, int port) {
        DataSource dataSource = createDataSource(hostName, port);
        return createNode(id, name, dataSource);
    }

    private Collection<Node> extractNodesFromCluster() {
        JdbcTemplate template = new JdbcTemplate(getDataSource().unwrap());
        return template.query(GET_NODES_SQL, rs -> {
            Collection<Node> nodes = new ArrayList<>();
            while (rs.next()) {
                String id = rs.getString("node_uuid");
                String name = rs.getString("node_name");
                if (GARB_NODE_NAME.equals(name)) continue;
                String host = rs.getString("node_incoming_address");
                int port = getPortFromHostAndPort(host);
                host = getHostFromHostAndPort(host);
                nodes.add(createNode(id, name, host, port));
            }
            return nodes;
        });
    }

    private Session.State getState(String command, String state) {
        command = toLowerCase(command);
        state = toLowerCase(state);
        if ("sleep".equals(command)) {
            return Session.State.INACTIVE;
        } else if (activeStates.contains(command)) {
            return Session.State.ACTIVE;
        } else if (blockedStates.contains(state)) {
            return Session.State.BLOCKED;
        } else if (waitStates.contains(state)) {
            return Session.State.WAITING;
        } else if ("killed".equals(state)) {
            return Session.State.KILLED;
        } else {
            return Session.State.INACTIVE;
        }
    }

    private boolean isSystem(String userName) {
        return "system user".equals(userName);
    }

    private Collection<Session> extractSessionsFromNode(Node node) {
        JdbcTemplate template = new JdbcTemplate(node.getDataSource().unwrap());
        return template.query(GET_SESSIONS_SQL, rs -> {
            Collection<Session> sessions = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                String nodeId = toIdentifier(node.getId() + "_" + rs.getString("id"));
                MySqlSession session = new MySqlSession(node, nodeId, id);
                session.setUserName(rs.getString("user"));
                session.setSchema(rs.getString("db"));
                session.setClientHostname(getHostFromHostAndPort(rs.getString("host")));
                session.setState(getState(rs.getString("command"), rs.getString("state")));
                session.setElapsed(Duration.ofMillis(rs.getLong("time_ms")));
                String info = rs.getString("info");
                if (isNotEmpty(info)) session.setStatement(Statement.create(info));
                session.setSystem(isSystem(rs.getString("user")));
                session.setInfo(rs.getString("state"));
                sessions.add(session);
            }
            return sessions;
        });
    }

    private static final String GET_NODES_SQL = "select * from mysql.wsrep_cluster_members order by node_name";
    private static final String GET_SESSIONS_SQL = "select * from information_schema.processlist";

    private static final Set<String> activeStates = new HashSet<>();
    private static final Set<String> blockedStates = new HashSet<>();
    private static final Set<String> waitStates = new HashSet<>();

    static {
        // commands
        activeStates.add("execute");
        activeStates.add("fetch");
        activeStates.add("long data");
        activeStates.add("prepare");
        activeStates.add("query");
        activeStates.add("drop db");
        activeStates.add("close stmt");
        activeStates.add("reset stmt");
        activeStates.add("set option");
        activeStates.add("delayed insert");
        // states
        activeStates.add("executing");
        activeStates.add("searching rows for update");
        activeStates.add("sending data");
        blockedStates.add("init");
        blockedStates.add("locked");
        waitStates.add("optimizing");
        waitStates.add("preparing");
        waitStates.add("reopen tables");
    }
}
