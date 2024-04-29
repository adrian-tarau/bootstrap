package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Identifiable;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * An UUID generator based on Twitter <a href="https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake">snowflake</a>.
 */
public class IdGenerator implements Identifiable<String> {

    private static final String DEFAULT_ID = "default";

    static final long BOUND = 8192;
    static final long EPOCH = ZonedDateTime.parse("2024-01-01T00:00:00Z").toInstant().toEpochMilli();

    static volatile int SERVER_ID = -1;

    private final long prefix;
    private final String id;
    private AtomicInteger sequence = new AtomicInteger();

    private static volatile IdGenerator GENERATOR;
    private static final Map<String, IdGenerator> GENERATORS = new ConcurrentHashMap<>();

    /**
     * Returns the instance of the next generator.
     * <p>
     * It should not be cached.
     */
    public static IdGenerator get() {
        if (GENERATOR == null || !GENERATOR.isValid()) {
            synchronized (IdGenerator.class) {
                if (GENERATOR == null || !GENERATOR.isValid()) {
                    GENERATOR = new IdGenerator(DEFAULT_ID, currentTimeMillis(), getServerId());
                }
            }
        }
        return GENERATOR;
    }

    /**
     * Returns the instance of the next generator with a given identifier.
     * <p>
     * It should not be cached.
     */
    public static IdGenerator get(String id) {
        if (isEmpty(id)) return get();
        id = toIdentifier(id);
        IdGenerator idGenerator = GENERATORS.get(id);
        if (idGenerator == null || !idGenerator.isValid()) {
            synchronized (IdGenerator.class) {
                idGenerator = GENERATORS.get(id);
                if (idGenerator == null || !idGenerator.isValid()) {
                    idGenerator = new IdGenerator(id, currentTimeMillis(), getServerId());
                    GENERATORS.put(id, idGenerator);
                }
            }
        }
        return idGenerator;
    }

    @Override
    public String getId() {
        return id;
    }


    /**
     * Returns the start of the id range for a given instance.
     * <p>
     * All keys generated after this time stamp start at this range.
     *
     * @param instant the instance
     * @return the start of the range
     */
    public static long rangeStart(Instant instant) {
        requireNonNull(instant);
        long value = instant.toEpochMilli() - EPOCH;
        value <<= 23;
        value &= 0x7fff_ffff_ffff_ffffL;
        return value;
    }

    IdGenerator(String id, long timestamp, int machineId) {
        this.id = isEmpty(id) ? DEFAULT_ID : id;
        prefix = generatePrefix(timestamp, machineId);
    }

    /**
     * Returns the next (sortable) UUID.
     *
     * @return a positive integer
     */
    public long next() {
        int next = sequence.getAndIncrement();
        if (next >= BOUND) {
            return IdGenerator.get().next();
        } else {
            return prefix + next;
        }
    }

    /**
     * Returns the next (sortable) UUID as a string.
     *
     * @return a non-null String
     */
    public String nextAsString() {
        long id = next();
        return Long.toString(id, Character.MAX_RADIX);
    }

    private boolean isValid() {
        return sequence.get() < BOUND;
    }

    private long generatePrefix(long timestamp, long machineId) {
        long value = timestamp - EPOCH;
        value <<= 10;
        value += machineId;
        value <<= 13;
        value &= 0x7fff_ffff_ffff_ffffL;
        return value;
    }

    private static int getServerId() {
        if (SERVER_ID > 0) return SERVER_ID;
        try {
            byte[] address = InetAddress.getLocalHost().getAddress();
            int addressLength = address.length;
            int serverId = (int) address[addressLength - 1] & 0xFF | (int) address[addressLength - 2] & 0xFF << 8
                    | (int) address[addressLength - 3] & 0xFF << 16;
            SERVER_ID = serverId % 1024;
        } catch (UnknownHostException e) {
            LoggerFactory.getLogger(IdGenerator.class).warn("Failed to extract server id: " + ExceptionUtils.getRootCauseMessage(e));
            SERVER_ID = 0;
        }
        return SERVER_ID;
    }
}
