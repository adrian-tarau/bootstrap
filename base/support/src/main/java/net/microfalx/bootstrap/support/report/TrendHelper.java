package net.microfalx.bootstrap.support.report;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Various helper methods for trends.
 */
public class TrendHelper {

    /**
     * Aggregates values by the given key mapper and sums up the values mapped by the value mapper.
     *
     * @param values      the values
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @param <S>         the object type
     * @return the aggregated map
     */
    public <S> Map<String, Long> aggregateLong(Collection<S> values, Function<S, String> keyMapper, Function<S, Long> valueMapper) {
        return values.stream().collect(Collectors.groupingBy(
                naMapper(keyMapper), Collectors.summingLong(valueMapper::apply)
        ));
    }

    /**
     * Aggregates values by the given key mapper and sums up the values mapped by the value mapper.
     *
     * @param values      the values
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @param <S>         the object type
     * @return the aggregated map
     */
    public <S> Map<String, Integer> aggregateInt(Collection<S> values, Function<S, String> keyMapper, Function<S, Integer> valueMapper) {
        return values.stream().collect(Collectors.groupingBy(
                naMapper(keyMapper), Collectors.summingInt(valueMapper::apply)
        ));
    }

    /**
     * Counts occurrences of values mapped by the given key mapper.
     *
     * @param values    the values
     * @param keyMapper the key mapper
     * @param <S>       the object type
     * @return the map of occurrences
     */
    public <S> Map<String, Long> count(Collection<S> values, Function<S, String> keyMapper) {
        return values.stream().collect(Collectors.groupingBy(
                naMapper(keyMapper), Collectors.counting()
        ));
    }

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

    private <S> Function<S, String> naMapper(Function<S, String> keyMapper) {
        return s -> StringUtils.defaultIfEmpty(keyMapper.apply(s), StringUtils.NA_STRING);
    }
}
