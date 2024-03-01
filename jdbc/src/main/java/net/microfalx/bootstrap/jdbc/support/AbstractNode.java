package net.microfalx.bootstrap.jdbc.support;

import java.time.LocalDateTime;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for a database node.
 */
public abstract class AbstractNode implements Node {

    private final AbstractDatabase database;
    private final String id;
    private final String name;

    private final DataSource dataSource;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime modifiedAt = LocalDateTime.now();

    private volatile State state = State.UP;

    public AbstractNode(AbstractDatabase database, String id, String name, DataSource dataSource) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        this.database = database;
        this.id = id;
        this.name = name;
        this.dataSource = dataSource;
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
    public final State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("dataSource=" + dataSource)
                .toString();
    }
}
