package net.microfalx.bootstrap.support.report;

import net.microfalx.lang.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Various helper methods for trends.
 */
public class TrendHelper {

    public <M extends TimeAwareMetrics<M>, T extends Identifiable<String>> Collection<T> getTypes(Collection<M> trends, Function<M, Collection<T>> mapper) {
        Map<String, T> types = new HashMap<>();
        for (M trend : trends) {
            for (T type : mapper.apply(trend)) {
                types.computeIfAbsent(type.getId(), s -> type);
            }
        }
        return types.values();
    }

    public <M extends TimeAwareMetrics<M>, T extends TimeAwareMetrics<T>> Collection<T> getMetrics(Collection<M> trends, Function<M, T> mapper) {
        Collection<T> metrics = new ArrayList<>();
        for (M trend : trends) {
            try {
                T value = mapper.apply(trend);
                if (value != null) metrics.add(value.updateInterval(trend.getStartTime(), trend.getEndTime()));
            } catch (IllegalArgumentException e) {
                // ignore "cannot find this id"
            }
        }
        return metrics;
    }
}
