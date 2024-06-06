package net.microfalx.bootstrap.jdbc.support;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.createJdbcUri;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.describe;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TimeUtils.FIVE_MINUTE;
import static net.microfalx.lang.TimeUtils.TEN_SECONDS;

/**
 * Base class for a database.
 */
public abstract class AbstractDatabase extends AbstractNode implements Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final long REFRESH_NODES_INTERVAL = TimeUtils.ONE_MINUTE;
    public static final int UNAVAILABLE_PORT = -1;

    transient DatabaseService databaseService;

    private volatile long lastNodesUpdate = 0;
    private volatile Map<String, Node> nodes = Collections.emptyMap();
    private volatile Metrics metrics;

    public AbstractDatabase(DatabaseService databaseService, String id, String name, DataSource dataSource) {
        super(null, id, name, dataSource);
        requireNonNull(databaseService);
        this.databaseService = databaseService;
    }

    @Override
    public Database getDatabase() {
        return this;
    }

    @Override
    public final Collection<Node> getNodes() {
        if (shouldUpdateNodes()) {
            synchronized (this) {
                if (shouldUpdateNodes()) {
                    lastNodesUpdate = currentTimeMillis();
                    try {
                        Collection<Node> extractedNodes = timeCallable("Extract Nodes", this::extractNodes);
                        copyNodeAttributes(extractedNodes);
                        nodes = extractedNodes.stream().collect(Collectors.toMap(node -> toIdentifier(node.getId()), node -> node));
                    } catch (CannotGetJdbcConnectionException e) {
                        LOGGER.debug("Failed to extract database nodes for " + describe(this) + ", database is not available, root cause: " + getRootCauseMessage(e));
                    } catch (Exception e) {
                        LOGGER.error("Failed to extract database nodes for " + describe(this), e);
                    }
                }
            }
        }
        return unmodifiableCollection(nodes.values());
    }

    @Override
    public Optional<Node> getNode(String id) {
        requireNotEmpty(id);
        getNodes();
        return ofNullable(nodes.get(toIdentifier(id)));
    }

    @Override
    public final Collection<Session> getSessions() {
        return timeCallable("Extract Sessions", this::extractSessions);
    }

    @Override
    public final Collection<Transaction> getTransactions() {
        return timeCallable("Extract Transactions", this::extractTransactions);
    }

    @Override
    public Collection<Statement> getStatements(LocalDateTime start, LocalDateTime end) {
        return timeCallable("Extract Statements", () -> extractStatements(start, end));
    }

    /**
     * Returns the nodes from the database.
     *
     * @return a non-null instance
     * @throws SQLException if the nodes cannot be extracted
     */
    protected abstract Collection<Node> extractNodes() throws SQLException;

    /**
     * Returns the sessions from the database.
     *
     * @return a non-null instance
     * @throws SQLException if the sessions cannot be extracted
     */
    protected abstract Collection<Session> extractSessions() throws SQLException;

    /**
     * Returns the transactions from the database.
     *
     * @return a non-null instance
     * @throws SQLException if the transactions cannot be extracted
     */
    protected abstract Collection<Transaction> extractTransactions() throws SQLException;

    /**
     * Returns the statements executed between a given interval.
     *
     * @param start the start time
     * @param end   the end time
     * @return a non-null instance
     */
    protected abstract Collection<Statement> extractStatements(LocalDateTime start, LocalDateTime end);

    /**
     * Returns the data source with a given identifier.
     *
     * @param id the identifier
     * @return the data source, null if it does not exist
     */
    protected final DataSource findDataSource(String id) {
        return databaseService.findDataSource(id).orElse(null);
    }

    /**
     * Registers a data source.
     *
     * @param dataSource the data source
     */
    protected final void registerDataSource(DataSource dataSource) {
        databaseService.registerDataSource(dataSource);
    }

    /**
     * Creates a data source for a given host and port.
     *
     * @param hostname the host name
     * @param port     the port
     * @return the data source
     */
    protected final DataSource createDataSource(String hostname, int port) {
        String id = getNodeDataSourceId(hostname);
        DataSource dataSource = findDataSource(id);
        if (dataSource != null) return dataSource;
        URI uri = DatabaseUtils.getURI(getDataSource());
        uri = createJdbcUri(replaceHostAndPort(uri, hostname, port));
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(10);
        config.setPoolName(hostname);
        config.setJdbcUrl(uri.toASCIIString());
        config.setUsername(getDataSource().getUserName());
        config.setPassword(getDataSource().getPassword());
        config.setConnectionTimeout(TEN_SECONDS);
        config.setIdleTimeout(FIVE_MINUTE);
        config.setInitializationFailTimeout(-1);
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        dataSource = DataSource.create(id, hostname, hikariDataSource).withUri(uri)
                .withUserName(getDataSource().getUserName())
                .withPassword(getDataSource().getPassword())
                .withNode(true);
        registerDataSource(dataSource);
        return dataSource;
    }

    /**
     * Returns the data source identifier for a node.
     *
     * @param hostname the hostname (or IP)
     * @return a non-null instance
     */
    protected final String getNodeDataSourceId(String hostname) {
        return toIdentifier(getDatabase().getId() + "_" + hostname);
    }

    /**
     * Returns the metrics group for this database.
     *
     * @return a non-null instance
     */
    protected final Metrics getMetrics() {
        if (metrics == null) metrics = DatabaseUtils.METRICS.withGroup(getType().name());
        return metrics;
    }

    /**
     * Times a database access.
     *
     * @param name     the name of the timer
     * @param supplier the supplier
     */
    protected final <T> T time(String name, Supplier<T> supplier) {
        return getMetrics().time(name, supplier);
    }

    /**
     * Times a database access.
     *
     * @param name     the name of the timer
     * @param callable the callable
     */
    protected final <T> T timeCallable(String name, Callable<T> callable) {
        return getMetrics().timeCallable(name, callable);
    }

    /**
     * Creates a statement if exists.
     *
     * @param node      the node where the statement was executed
     * @param statement the statement
     * @param userName  name the user name
     * @return a non-null instance if exists, null otherwise
     */
    protected final Statement createStatement(Node node, String statement, String userName) {
        return isNotEmpty(statement) ? Statement.create(node, statement, userName) : null;
    }

    @Override
    protected void doValidate() {
        super.doValidate();
        for (Node node : nodes.values()) {
            try {
                node.validate();
            } catch (Exception e) {
                // ignore if it happens
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDatabase that = (AbstractDatabase) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType());
    }

    /**
     * Extracts the host part from a hostname and an option port
     *
     * @param hostname the host name or hostname and port (separated by ":"), can be null
     * @return the hostname or null if it was null
     */
    public static String getHostFromHostAndPort(String hostname) {
        if (hostname == null) return null;
        String[] parts = splitHostAndPort(hostname);
        return parts.length > 0 ? parts[0] : EMPTY_STRING;
    }

    /**
     * Extracts the host part from a hostname and an option port
     *
     * @param hostname the host name or hostname and port (separated by ":"), can be null
     * @return the port or {@link #UNAVAILABLE_PORT} if missing or invalid
     */
    public static int getPortFromHostAndPort(String hostname) {
        if (hostname == null) return UNAVAILABLE_PORT;
        String[] parts = splitHostAndPort(hostname);
        try {
            return parts.length == 2 ? Integer.parseInt(parts[1]) : UNAVAILABLE_PORT;
        } catch (NumberFormatException e) {
            return UNAVAILABLE_PORT;
        }
    }

    /**
     * Replaces the hostname and port from the URI.
     *
     * @param uri      the original URI
     * @param hostName the new hostname
     * @param port     the new port
     * @return a new URI
     */
    public static URI replaceHostAndPort(URI uri, String hostName, int port) {
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), hostName, port, uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    /**
     * Returns whether the host points to the local host.
     *
     * @param host the host
     * @return {@code true} if local host, {@code false} otherwise
     */
    public static boolean isLocalHost(String host) {
        if (host == null) return true;
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("::1");
    }

    private void copyNodeAttributes(Collection<Node> nodes) {
        for (Node node : nodes) {
            Node prevNode = this.nodes.get(node.getId());
            if (prevNode != null) {
                ((AbstractNode) node).copyFrom((AbstractNode) node);
            }
        }
    }

    private boolean shouldUpdateNodes() {
        return nodes.isEmpty() || TimeUtils.millisSince(lastNodesUpdate) > REFRESH_NODES_INTERVAL;
    }

    private static String[] splitHostAndPort(String hostname) {
        if (hostname == null) return EMPTY_STRING_ARRAY;
        if (hostname.contains("]") || hostname.contains("::")) {
            String[] parts = split(hostname, "]", true);
            if (parts.length != 2) return new String[]{hostname};
            if (parts[0].startsWith("[")) parts[0] = parts[0].substring(1);
            if (parts.length > 1 && parts[1].startsWith(":")) parts[1] = parts[1].substring(1);
            return parts;
        } else {
            String[] parts = split(hostname, ":", true);
            return parts.length > 2 ? new String[]{hostname} : parts;
        }
    }
}
