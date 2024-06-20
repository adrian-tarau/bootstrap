package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.*;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.IOUtils.closeQuietly;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * Base class for a database node.
 */
public abstract class AbstractNode implements Node {

    private static final String VALIDATE_SQL = "select 1";

    transient AbstractDatabase database;
    String databaseId;
    private final String id;
    private final String name;
    private String displayName;

    private transient DataSource dataSource;
    String dataSourceId;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime modifiedAt = LocalDateTime.now();

    private volatile State state = State.UP;
    private volatile Boolean available;
    private volatile String validationError;
    private volatile long lastValidationTime = TimeUtils.oneDayAgo();

    public AbstractNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        this.database = database;
        this.databaseId = database != null ? database.getId() : null;
        this.id = id;
        this.name = name;
        this.dataSource = dataSource;
        this.dataSourceId = dataSource.getId();
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return StringUtils.defaultIfEmpty(displayName, name);
    }

    protected final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public final String getDescription() {
        return dataSource.getDescription();
    }

    @Override
    public final DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public boolean isAvailable() {
        if (available == null || millisSince(lastValidationTime) > AVAILABILITY_INTERVAL.toMillis()) {
            validate();
        }
        return available;
    }

    @Override
    public final void validate() {
        doValidate();
    }

    @Override
    public final String getValidationError() {
        return validationError;
    }

    @Override
    public final ZoneId getZoneId() {
        return dataSource.getZoneId();
    }

    @Override
    public final State getState() {
        return state;
    }

    public void setState(State state) {
        requireNotEmpty(state);
        this.state = state;
        if (state == State.UP || state == State.STANDBY) {
            this.available = true;
            this.validationError = null;
        } else {
            this.available = false;
        }
        this.lastValidationTime = currentTimeMillis();
    }

    public void setState(State state, String validationError) {
        setState(state);
        this.validationError = validationError;
    }

    @Override
    public final String getHostname() {
        return getDataSource().getHostname();
    }

    @Override
    public final int getPort() {
        return getDataSource().getPort();
    }

    @Override
    public final LocalDateTime getStartedAt() {
        return startedAt;
    }

    public final void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public final LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public final void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public final LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public final void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNode that = (AbstractNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected void copyFrom(AbstractNode node) {
        available = node.available;
        state = node.state;
        validationError = node.validationError;
        lastValidationTime = node.lastValidationTime;
    }

    protected boolean doIsAvailable() {
        boolean available = isAvailableOverNetwork();
        if (available) {
            available = isAvailableOverSQL();
        }
        return available;
    }

    protected boolean isAvailableOverNetwork() {
        boolean availableOverNetwork = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(getHostname());
            availableOverNetwork = inetAddress.isReachable((int) PING_TIMEOUT.toMillis());
        } catch (Exception e) {
            validationError = e.getMessage();
        }
        return availableOverNetwork;
    }

    protected boolean isAvailableOverSQL() {
        boolean availableOverSql = false;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getDataSource().getConnection();
            statement = connection.prepareStatement(VALIDATE_SQL);
            availableOverSql = true;
        } catch (Exception e) {
            validationError = e.getMessage();
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
        }
        return availableOverSql;
    }

    protected void doValidate() {
        available = METRICS.time("Validate", this::doIsAvailable);
        if (!available) state = State.DOWN;
    }

    void close() {
        DatabaseService databaseService = ((AbstractDatabase) getDatabase()).databaseService;
        databaseService.releaseDataSource(dataSource);
        if (this instanceof Database database) {
            for (Node node : database.getNodes()) {
                databaseService.releaseDataSource(node.getDataSource());
            }
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("dataSource=" + dataSource)
                .toString();
    }
}
