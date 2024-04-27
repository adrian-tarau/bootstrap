package net.microfalx.bootstrap.search;

import net.microfalx.lang.Nameable;
import net.microfalx.lang.TimeUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds the trend for field values per 5 minute intervals.
 */
public class FieldTrend implements Nameable {

    private static final long INTERVAL = TimeUtils.FIVE_MINUTE;

    private final String field;
    private final Map<Long, Counts> values = new HashMap<>();

    public FieldTrend(String field) {
        requireNonNull(field);
        this.field = field;
    }

    @Override
    public String getName() {
        return field;
    }

    /**
     * Returns the field values at given timestamps (instants).
     *
     * @return a non-null instance
     */
    public Map<Long, Counts> getValues() {
        return unmodifiableMap(values);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FieldTrend.class.getSimpleName() + "[", "]")
                .add("field='" + field + "'")
                .add("values=" + values.size())
                .toString();
    }

    void increment(long timestamp, String value) {
        timestamp = (timestamp / INTERVAL) * INTERVAL;
        Counts counts = values.computeIfAbsent(timestamp, Counts::new);
        counts.increment(value);
    }

    void merge(FieldTrend trend) {
        for (Map.Entry<Long, Counts> entry : trend.values.entrySet()) {
            values.merge(entry.getKey(), entry.getValue(), (oldCounts, newCounts) -> {
                oldCounts.merge(newCounts);
                return oldCounts;
            });
        }
    }

    public static class Counts {

        private final long timestamp;
        private final Map<String, MutableInt> counts = new HashMap<>();

        Counts(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Map<String, ? extends Number> getCounts() {
            return unmodifiableMap(counts);
        }

        void increment(String value) {
            MutableInt counter = counts.computeIfAbsent(value, s -> new MutableInt());
            counter.increment();
        }

        void merge(Counts counts) {
            for (Map.Entry<String, MutableInt> entry : counts.counts.entrySet()) {
                this.counts.merge(entry.getKey(), entry.getValue(), (m1, m2) -> new MutableInt(m1.intValue() + m2.intValue()));
            }
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Counts.class.getSimpleName() + "[", "]")
                    .add("timestamp=" + timestamp)
                    .add("counts=" + counts.size())
                    .toString();
        }
    }
}
