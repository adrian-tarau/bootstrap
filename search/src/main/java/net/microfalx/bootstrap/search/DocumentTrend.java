package net.microfalx.bootstrap.search;

import net.microfalx.lang.TimeUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.unmodifiableMap;

/**
 * Holds the trend for documents values per 5 minute intervals.
 */
public class DocumentTrend {

    private static final long INTERVAL = TimeUtils.FIVE_MINUTE;

    private final Map<Long, MutableInt> values = new HashMap<>();

    public Map<Long, ? extends Number> getCounts() {
        return unmodifiableMap(values);
    }

    public int getCount() {
        return values.size();
    }

    void increment(long timestamp) {
        timestamp = (timestamp / INTERVAL) * INTERVAL;
        MutableInt counter = values.computeIfAbsent(timestamp, s -> new MutableInt());
        counter.increment();
    }

    void merge(DocumentTrend trend) {
        for (Map.Entry<Long, MutableInt> entry : trend.values.entrySet()) {
            this.values.merge(entry.getKey(), entry.getValue(), (m1, m2) -> new MutableInt(m1.intValue() + m2.intValue()));
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DocumentTrend.class.getSimpleName() + "[", "]")
                .add("values=" + values.size())
                .toString();
    }
}
