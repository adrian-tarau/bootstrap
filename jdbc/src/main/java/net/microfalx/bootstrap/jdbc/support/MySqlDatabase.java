package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.bootstrap.jdbc.support.Transaction.IsolationLevel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.singleton;
import static net.microfalx.lang.EnumUtils.fromName;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.StringUtils.toLowerCase;
import static net.microfalx.lang.TimeUtils.toZonedDateTimeSameInstant;

public class MySqlDatabase extends AbstractDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final int TABLE_NOT_FOUND_ERROR = 1146;
    private static final int ACCESS_DENIED_ERROR = 1227;
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
        Collection<Transaction> transactions = new ArrayList<>();
        Collection<Node> nodes = getNodes();
        for (Node node : nodes) {
            try {
                transactions.addAll(time("Extract Transactions - " + node.getName(), () -> extractTransactionsFromNode(node)));
            } catch (BadSqlGrammarException e) {
                int errorCode = net.microfalx.lang.ExceptionUtils.getSQLErrorCode(e);
                if (errorCode != ACCESS_DENIED_ERROR) LOGGER.error("Failed to extract transactions", e);
            }
        }
        return transactions;
    }

    @Override
    protected Collection<Statement> extractStatements(LocalDateTime start, LocalDateTime end) {
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
                String nodeId = toIdentifier(node.getId() + "_" + id);
                MySqlSession session = new MySqlSession(node, nodeId, id);
                session.setUserName(rs.getString("user"));
                session.setSchema(rs.getString("db"));
                session.setClientHostname(getHostFromHostAndPort(rs.getString("host")));
                session.setState(getState(rs.getString("command"), rs.getString("state")));
                session.setElapsed(Duration.ofMillis(rs.getLong("time_ms")));
                session.setStartedAt(LocalDateTime.now().minusSeconds(session.getElapsed().toSeconds()).atZone(getZoneId()));
                session.setCreatedAt(session.getCreatedAt());
                String info = rs.getString("info");
                session.setStatement(createStatement(node, info, session.getUserName()));
                session.setSystem(isSystem(rs.getString("user")));
                session.setInfo(rs.getString("state"));
                sessions.add(session);
            }
            return sessions;
        });
    }

    private Collection<Transaction> extractTransactionsFromNode(Node node) {
        JdbcTemplate template = new JdbcTemplate(node.getDataSource().unwrap());
        return template.query(GET_TRANSACTIONS_SQL, rs -> {
            Collection<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("trx_id");
                long threadId = rs.getLong("trx_mysql_thread_id");
                String nodeId = toIdentifier(node.getId() + "_" + id + "_" + threadId);
                MySqlTransaction transaction = new MySqlTransaction(node, nodeId, id, threadId);
                transaction.setStartedAt(toZonedDateTimeSameInstant(rs.getTimestamp("trx_started"), getZoneId()));
                transaction.setLockStartedAt(toZonedDateTimeSameInstant(rs.getTimestamp("trx_wait_started"), getZoneId()));
                transaction.setWeight(rs.getInt("trx_weight"));
                transaction.setStatement(createStatement(node, rs.getString("trx_query"), rs.getString("user")));
                transaction.setOperation(rs.getString("trx_operation_state"));
                transaction.setTablesInUseCount(rs.getInt("trx_tables_in_use"));
                transaction.setLockedRowCount(rs.getInt("trx_tables_locked"));
                transaction.setLockedRowCount(rs.getInt("trx_rows_locked"));
                transaction.setModifiedRowCount(rs.getInt("trx_rows_modified"));
                transaction.setReadOnly(rs.getInt("trx_is_read_only") != 0);
                transaction.setIsolationLevel(fromName(IsolationLevel.class, rs.getString("trx_isolation_level"), IsolationLevel.READ_COMMITTED));
                transaction.setState(fromName(Transaction.State.class, rs.getString("trx_state"), Transaction.State.RUNNING));
                transactions.add(transaction);
            }
            return transactions;
        });
    }

    private static final String GET_NODES_SQL = "select * from mysql.wsrep_cluster_members order by node_name";
    private static final String GET_SESSIONS_SQL = "select * from information_schema.processlist where CONNECTION_ID() <> id";
    private static final String GET_TRANSACTIONS_SQL = "SELECT t.*, p.`USER` FROM information_schema.innodb_trx t" +
            "\n  left join information_schema.processlist p on t.trx_mysql_thread_id = p.id";

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
