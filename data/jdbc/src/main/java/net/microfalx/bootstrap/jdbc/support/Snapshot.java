package net.microfalx.bootstrap.jdbc.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.microfalx.lang.Identifiable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Character.MAX_RADIX;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A class which carries a snapshot of all metrics for a database
 */
public class Snapshot implements Identifiable<String> {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final String id;
    private transient Database database;
    private String databaseId;
    private Collection<Node> nodes = new ArrayList<>();
    private Collection<Session> sessions = new ArrayList<>();
    private Collection<Transaction> transactions = new ArrayList<>();
    private int sessionsActiveCount;
    private int sessionsWaitingCount;
    private int sessionsBlockedCount;
    private int sessionsInactiveCount;
    private int sessionsKilledCount;
    private int transactionRunningCount;
    private int transactionBlockedCount;
    private int transactionCommittingCount;
    private int transactionRollingBackCount;
    private boolean incomplete;

    public Snapshot(Database database) {
        requireNonNull(database);
        this.database = database;
        this.databaseId = database.getId();
        this.id = database.getId() + "_" + Long.toString(currentTimeMillis(), MAX_RADIX) + "_" + COUNTER.getAndIncrement();
    }

    @Override
    public String getId() {
        return id;
    }

    public Database getDatabase() {
        return database;
    }

    public Collection<Node> getNodes() {
        return unmodifiableCollection(nodes);
    }

    void setNodes(Collection<Node> nodes) {
        this.nodes = nodes;
    }

    public Collection<Session> getSessions() {
        return unmodifiableCollection(sessions);
    }

    public int getSessionsActiveCount() {
        return sessionsActiveCount;
    }

    public int getSessionsWaitingCount() {
        return sessionsWaitingCount;
    }

    public int getSessionsBlockedCount() {
        return sessionsBlockedCount;
    }

    public int getSessionsInactiveCount() {
        return sessionsInactiveCount;
    }

    public int getSessionsKilledCount() {
        return sessionsKilledCount;
    }

    void setSessions(Collection<Session> sessions) {
        this.sessions = sessions;
        for (Session session : sessions) {
            switch (session.getState()) {
                case ACTIVE -> sessionsActiveCount++;
                case BLOCKED -> sessionsBlockedCount++;
                case WAITING -> sessionsWaitingCount++;
                case INACTIVE -> sessionsInactiveCount++;
                case KILLED -> sessionsKilledCount++;
            }
        }
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    public int getTransactionRunningCount() {
        return transactionRunningCount;
    }

    public int getTransactionBlockedCount() {
        return transactionBlockedCount;
    }

    public int getTransactionCommittingCount() {
        return transactionCommittingCount;
    }

    public int getTransactionRollingBackCount() {
        return transactionRollingBackCount;
    }

    void setTransactions(Collection<Transaction> transactions) {
        this.transactions = transactions;
        for (Transaction transaction : transactions) {
            switch (transaction.getState()) {
                case RUNNING -> transactionRunningCount++;
                case LOCK_WAIT -> transactionBlockedCount++;
                case COMMITTING -> transactionCommittingCount++;
                case ROLLING_BACK -> transactionRollingBackCount++;
            }
        }
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    public void serialize(OutputStream outputStream) {
        requireNotEmpty(outputStream);
        Output output = new Output(outputStream);
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.writeClassAndObject(output, this);
        output.close();
    }

    public static Snapshot deserialize(InputStream inputStream) {
        requireNotEmpty(inputStream);
        Input input = new Input(inputStream);
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return (Snapshot) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Snapshot.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("database=" + database.getName())
                .add("nodes=" + nodes.size())
                .add("sessions=" + sessions.size())
                .add("transactions=" + transactions.size())
                .toString();
    }
}
