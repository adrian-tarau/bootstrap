package net.microfalx.bootstrap.jdbc.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for a database session.
 */
public abstract class AbstractSession implements Session {

    private final String id;
    private final Node node;
    private String userName;
    private String schema;
    private State state = State.INACTIVE;
    private String transactionId;
    private String clientHostname;
    private LocalDateTime startedAt;
    private Duration elapsed;
    private LocalDateTime createdAt;
    private transient Statement statement;
    private String statementId;
    private String info;
    private boolean system;

    public AbstractSession(Node node, String id) {
        requireNonNull(node);
        requireNonNull(id);
        this.node = node;
        this.id = id;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Node getNode() {
        return node;
    }

    @Override
    public final String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public final State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
        if (statement != null) this.statementId = statement.getId();
    }

    @Override
    public final String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public final String getClientHostname() {
        return clientHostname;
    }

    public void setClientHostname(String clientHostname) {
        this.clientHostname = clientHostname;
    }

    @Override
    public final Duration getElapsed() {
        if (startedAt != null) {
            return Duration.between(startedAt, LocalDateTime.now());
        } else {
            return elapsed;
        }
    }

    public void setElapsed(Duration elapsed) {
        this.elapsed = elapsed;
    }

    @Override
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public final LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreated(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public final boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("node=" + node)
                .add("userName='" + userName + "'")
                .add("state=" + state)
                .add("transactionId='" + transactionId + "'")
                .add("clientHostname='" + clientHostname + "'")
                .add("elapsed=" + elapsed)
                .add("system=" + system)
                .toString();
    }
}
