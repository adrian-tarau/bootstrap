package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.ExceptionUtils;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An UUID generator based on Twitter <a href="https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake">snowflake</a>.
 */
public class IdGenerator {

    static final long BOUND = 8192;
    static final long EPOCH = ZonedDateTime.parse("2024-01-01T00:00:00Z").toInstant().toEpochMilli();

    static volatile int SERVER_ID = -1;

    private final long prefix;
    private AtomicInteger sequence = new AtomicInteger();

    private static volatile IdGenerator GENERATOR;

    /**
     * Returns the instance of the next generator.
     * <p>
     * It should not be cached.
     */
    public static IdGenerator get() {
        if (GENERATOR == null || !GENERATOR.isValid()) {
            synchronized (IdGenerator.class) {
                if (GENERATOR == null || !GENERATOR.isValid()) {
                    GENERATOR = new IdGenerator(System.currentTimeMillis(), getServerId());
                }
            }
        }
        return GENERATOR;
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

    IdGenerator(long timestamp, int machineId) {
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
