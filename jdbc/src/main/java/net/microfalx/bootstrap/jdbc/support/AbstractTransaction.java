package net.microfalx.bootstrap.jdbc.support;

import java.time.ZonedDateTime;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for a database transaction.
 */
public abstract class AbstractTransaction implements Transaction {

    private final String id;
    private final Node node;

    private State state = State.RUNNING;
    private ZonedDateTime startedAt;
    private ZonedDateTime lockStartedAt;
    private Integer weight;
    private Statement statement;
    private String statementId;
    private String operation;

    private int tablesInUseCount;
    private int tablesLockedCount;
    private int lockedRowCount;
    private int modifiedRowCount;
    private IsolationLevel isolationLevel = IsolationLevel.READ_COMMITTED;
    private boolean readOnly;

    public AbstractTransaction(Node node, String id) {
        requireNonNull(node);
        requireNonNull(id);
        this.id = id;
        this.node = node;
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
    public final State getState() {
        return state;
    }

    public final void setState(State state) {
        requireNonNull(state);
        this.state = state;
    }

    @Override
    public final ZonedDateTime getStartedAt() {
        return startedAt;
    }

    public final void setStartedAt(ZonedDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public final ZonedDateTime getLockStartedAt() {
        return lockStartedAt;
    }

    public final void setLockStartedAt(ZonedDateTime lockStartedAt) {
        this.lockStartedAt = lockStartedAt;
    }

    @Override
    public final Integer getWeight() {
        return weight;
    }

    public final void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public final Statement getStatement() {
        return statement;
    }

    public final void setStatement(Statement statement) {
        this.statement = statement;
        if (statement != null) statementId = statement.getId();
    }

    @Override
    public final String getOperation() {
        return operation;
    }

    public final void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public final int getTablesInUseCount() {
        return tablesInUseCount;
    }

    public final void setTablesInUseCount(int tablesInUseCount) {
        this.tablesInUseCount = tablesInUseCount;
    }

    @Override
    public final int getTablesLockedCount() {
        return tablesLockedCount;
    }

    public final void setTablesLockedCount(int tablesLockedCount) {
        this.tablesLockedCount = tablesLockedCount;
    }

    @Override
    public final int getLockedRowCount() {
        return lockedRowCount;
    }

    public final void setLockedRowCount(int lockedRowCount) {
        this.lockedRowCount = lockedRowCount;
    }

    @Override
    public final int getModifiedRowCount() {
        return modifiedRowCount;
    }

    public final void setModifiedRowCount(int modifiedRowCount) {
        this.modifiedRowCount = modifiedRowCount;
    }

    @Override
    public final IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public final void setIsolationLevel(IsolationLevel isolationLevel) {
        requireNonNull(isolationLevel);
        this.isolationLevel = isolationLevel;
    }

    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }

    public final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("node=" + node)
                .add("state=" + state)
                .add("startedAt=" + startedAt)
                .add("lockStartedAt=" + lockStartedAt)
                .add("weight=" + weight)
                .add("statement=" + statement)
                .add("operation='" + operation + "'")
                .add("tablesInUseCount=" + tablesInUseCount)
                .add("tablesLockedCount=" + tablesLockedCount)
                .add("lockedRowCount=" + lockedRowCount)
                .add("modifiedRowCount=" + modifiedRowCount)
                .add("isolationLevel=" + isolationLevel)
                .add("readOnly=" + readOnly)
                .toString();
    }
}
