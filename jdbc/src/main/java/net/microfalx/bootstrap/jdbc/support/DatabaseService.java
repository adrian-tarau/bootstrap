package net.microfalx.bootstrap.jdbc.support;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zaxxer.hikari.HikariDataSource;
import net.microfalx.bootstrap.store.Store;
import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toMap;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ConcurrencyUtils.collectFutures;
import static net.microfalx.lang.ConcurrencyUtils.waitForFutures;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * A service which provides database specific support.
 */
@Service
public class DatabaseService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final long SESSION_REFRESH_INTERVAL = 5_000;
    private static final long STATEMENT_CACHE = 5_000;

    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, Database> databases = new ConcurrentHashMap<>();

    private volatile Map<String, Session> lastSessions = Collections.emptyMap();
    private final Cache<String, Statement> statements = CacheBuilder.newBuilder().maximumSize(STATEMENT_CACHE).softValues().build();
    private volatile long lastSessionExtractTime = TimeUtils.oneHourAgo();
    private final AtomicBoolean extractingSessions = new AtomicBoolean();

    @Autowired(required = false)
    private javax.sql.DataSource dataSource;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private StoreService storeService;

    private Store<String, Statement> statementStore;

    @Value("${bootstrap.application.name}")
    private String applicationName;

    /**
     * Returns the task executor for this service.
     *
     * @return a non-null instance
     */
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    /**
     * Returns registered data sources.
     *
     * @return a non-null instance
     */
    public Collection<DataSource> getDataSources() {
        return unmodifiableCollection(dataSources.values());
    }

    /**
     * Returns a data source by its identifier.
     *
     * @param id the identifier
     * @return the optional data source
     */
    public Optional<DataSource> findDataSource(String id) {
        requireNotEmpty(id);
        toIdentifier(id);
        return Optional.ofNullable(dataSources.get(id));
    }

    /**
     * Returns a data source by its identifier.
     *
     * @param id the identifier
     * @return the data source
     */
    public DataSource getDataSource(String id) {
        requireNotEmpty(id);
        toIdentifier(id);
        DataSource dataSource = dataSources.get(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("A data source with identifier '" + id + "' is not registered");
        }
        return dataSource;
    }

    /**
     * Returns registered databases.
     *
     * @return a non-null instance
     */
    public Collection<Database> getDatabase() {
        return unmodifiableCollection(databases.values());
    }

    /**
     * Returns a database by its identifier.
     *
     * @param id the identifier
     * @return the data source
     */
    public Database getDatabase(String id) {
        requireNotEmpty(id);
        id = toIdentifier(id);
        Database dataSource = databases.get(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("A database with identifier '" + id + "' is not registered");
        }
        return dataSource;
    }

    /**
     * Returns a collection of sessions from registered databases.
     *
     * @return a non-null instance
     */
    public Collection<Session> getSessions() {
        if ((this.lastSessions.isEmpty() || millisSince(lastSessionExtractTime) > SESSION_REFRESH_INTERVAL)
                && extractingSessions.compareAndSet(false, true)) {
            taskExecutor.execute(DATABASE.getTimer("Get Sessions").wrap(new ExtractSessions()));
        }
        return this.lastSessions.values();
    }

    /**
     * Returns a session by its identifier.
     *
     * @param id the session identifier
     * @return a non-null optional
     */
    public Optional<Session> findSession(String id) {
        requireNotEmpty(id);
        return Optional.ofNullable(lastSessions.get(id));
    }

    /**
     * Registers a data source to be tracked.
     *
     * @param dataSource the data source
     */
    public void registerDataSource(DataSource dataSource) {
        requireNonNull(dataSource);
        String id = toIdentifier(dataSource.getId());
        if (!dataSources.containsKey(id)) {
            LOGGER.info("Register data source '{}', name '{}'", dataSource.getId(), dataSource.getName());
        }
        dataSources.put(id, dataSource);
        if (!dataSource.isNode()) {
            Database database = createDatabase(dataSource);
            if (database != null) databases.put(id, database);
        }
    }

    /**
     * Returns a statement by its identifier.
     *
     * @param id the statement identifier
     * @return an optional
     */
    public Optional<Statement> getStatement(String id) {
        requireNonNull(id);
        Statement statement = statements.getIfPresent(id);
        if (statement != null) return Optional.of(statement);
        statement = statementStore.find(id);
        if (statement != null) statements.put(id, statement);
        return Optional.ofNullable(statement);
    }

    /**
     * Registers a new statement.
     *
     * @param statement the SQL statement
     * @return a statement
     */
    public Statement registerStatement(Statement statement) {
        ArgumentUtils.requireNonNull(statement);
        Statement prevStatement = statements.getIfPresent(statement.getId());
        if (prevStatement == null) {
            statementStore.add(statement);
            statements.put(statement.getId(), statement);
        }
        return prevStatement != null ? prevStatement : statement;
    }

    /**
     * Updates the data source properties with information
     *
     * @param dataSource the data source
     * @return an update data source
     */
    public DataSource updateProperties(DataSource dataSource) {
        javax.sql.DataSource unwrapped = dataSource.unwrap();
        if (unwrapped instanceof HikariDataSource hikariDataSource) {
            dataSource = dataSource.withUri(URI.create(hikariDataSource.getJdbcUrl())).withUserName(hikariDataSource.getUsername());
        } else {
            LOGGER.error("Unknown data source implementation: " + ClassUtils.getName(dataSource));
        }
        return dataSource;
    }

    /**
     * Returns nodes from all databases.
     *
     * @return a non-null instance
     */
    public Collection<Node> getNodes() {
        return databases.values().stream().flatMap(database -> database.getNodes().stream()).toList();
    }

    /**
     * Returns a node by its identifier.
     *
     * @param id the node identifier
     * @return a non-null optional
     */
    public Optional<Node> findNode(String id) {
        requireNotEmpty(id);
        for (Database database : databases.values()) {
            Optional<Node> node = database.getNode(id);
            if (node.isPresent()) return node;
        }
        return Optional.empty();
    }

    /**
     * Returns a database by its identifier.
     *
     * @param id the identifier
     * @return the data source
     */
    public Node getNode(String id) {
        requireNotEmpty(id);
        String finalId = toIdentifier(id);
        return databases.values().stream().filter(database -> database.getId().equals(finalId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("A database node with identifier '" + id + "' is not registered"));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (dataSource != null) {
            String name = defaultIfEmpty(applicationName, "Default");
            String id = toIdentifier(name);
            DataSource newDataSource = updateProperties(DataSource.create(id, name, dataSource)
                    .withDescription("The application database"));
            registerDataSource(newDataSource);
        }
        statementStore = storeService.registerStore(Store.Options.create("Database Statement"));
    }

    private Database createDatabase(DataSource dataSource) {
        URI uri = DatabaseUtils.getURI(dataSource);
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if (MYSQL_SCHEME.equals(scheme)) {
            return new MySqlDatabase(this, dataSource.getId(), dataSource.getName(), dataSource);
        } else if (VERTICA_SCHEME.equals(scheme)) {
            return new VerticaDatabase(this, dataSource.getId(), dataSource.getName(), dataSource);
        } else {
            LOGGER.error("Unknown JDBC scheme: " + scheme);
            return null;
        }
    }

    class ExtractSessions implements Runnable {

        @Override
        public void run() {
            lastSessionExtractTime = currentTimeMillis();
            try {
                Map<String, Session> sessions = new HashMap<>();
                Collection<Future<Collection<Session>>> futures = new ArrayList<>();
                for (Database database : databases.values()) {
                    futures.add(taskExecutor.submit(new ExtractSessionsForDatabase(database)));
                }
                int pendingTasks = waitForFutures(futures);
                if (pendingTasks > 0) LOGGER.warn("Incomplete list of sessions, pending tasks: " + pendingTasks);
                sessions.putAll(collectFutures(futures).stream().flatMap(Collection::stream)
                        .collect(toMap(Session::getId, session -> session)));
                DatabaseService.this.lastSessions = sessions;
            } finally {
                extractingSessions.set(false);
            }
        }
    }

    class ExtractSessionsForDatabase implements Callable<Collection<Session>> {

        private final Database database;

        public ExtractSessionsForDatabase(Database database) {
            this.database = database;
        }

        private void registerStatements(Collection<Session> sessions) {
            for (Session session : sessions) {
                if (session.getStatement() == null) continue;
                try {
                    registerStatement(session.getStatement());
                } catch (Exception e) {
                    LOGGER.error("Failed to analyze statement " + org.apache.commons.lang3.StringUtils
                            .abbreviate(session.getStatement().getContent(), 80), e);
                }
            }
        }

        @Override
        public Collection<Session> call() throws Exception {
            Collection<Session> sessions = database.getSessions();
            registerStatements(sessions);
            return sessions;
        }
    }
}
