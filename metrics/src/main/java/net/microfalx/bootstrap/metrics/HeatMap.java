package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A heatmap.
 */
@ToString
public final class HeatMap implements Identifiable<String>, Nameable {

    public static final int DEFAULT_MAXIMUM_LANES = 10;

    private final String id = MetricUtils.nextId("series");
    private final String name;
    private final List<Series> series;
    private final int maximumLanes;

    /**
     * Creates an empty series for a given metric and its values.
     *
     * @param name the name of the series
     * @return a non-null instance
     */
    public static HeatMap create(String name) {
        return new HeatMap(name, null, DEFAULT_MAXIMUM_LANES);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param series the iterable
     * @return a non-null instance
     */
    public static HeatMap create(String name, Iterable<Series> series) {
        return create(name, series, DEFAULT_MAXIMUM_LANES);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param series the iterable
     * @return a non-null instance
     */
    public static HeatMap create(String name, Iterable<Series> series, int maximumLanes) {
        return new HeatMap(name, series, maximumLanes);
    }

    /**
     * Creates heat maps out of matrices with maximum 10 lanes.
     *
     * @param matrices the matrices
     * @return a non-null collection
     */
    public static Collection<HeatMap> create(Collection<Matrix> matrices) {
        return create(matrices, DEFAULT_MAXIMUM_LANES);
    }

    /**
     * Creates heat maps out of matrices.
     *
     * @param matrices     the matrices
     * @param maximumLanes the maximum number of lanes, 0 or less for all
     * @return a non-null collection
     */
    public static Collection<HeatMap> create(Collection<Matrix> matrices, int maximumLanes) {
        return new MatrixBuilder(matrices, maximumLanes).build();
    }

    HeatMap(String name, Iterable<Series> series, int maximumLanes) {
        requireNonNull(name);
        this.name = name;
        this.maximumLanes = maximumLanes;
        this.series = trimSeries(toList(series));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Return a list of series.
     *
     * @return a non-null instance
     */
    public List<Series> getSeries() {
        return unmodifiableList(series);
    }

    private List<Series> trimSeries(List<Series> series) {
        if (maximumLanes <= 0 || series.size() < maximumLanes) return series;
        List<Series> sortedSeries = new ArrayList<>(series);
        sortedSeries.sort(Comparator.comparing(Series::getWeight).reversed());
        sortedSeries = sortedSeries.subList(0, maximumLanes);
        Set<String> ids = sortedSeries.stream().map(Series::getId).collect(Collectors.toSet());
        return series.stream().filter(s -> ids.contains(s.getId())).collect(Collectors.toList());
    }

    static class MatrixBuilder {

        private final Collection<Matrix> matrices;
        private final int maximumLanes;
        private final Map<String, Map<String, Map<Long, Value>>> series = new HashMap<>();

        MatrixBuilder(Collection<Matrix> matrices, int maximumLanes) {
            requireNonNull(matrices);
            this.matrices = matrices;
            this.maximumLanes = maximumLanes;
        }

        private void append(Matrix matrix) {
            Metric metric = matrix.getMetric();
            for (String label : metric.getLabels()) {
                String labelValue = metric.getLabel(label);
                for (Value value : matrix.getValues()) {
                    Map<String, Map<Long, Value>> perLabelMap = series.computeIfAbsent(label, l -> new HashMap<>());
                    Map<Long, Value> values = perLabelMap.computeIfAbsent(labelValue, s -> new HashMap<>());
                    Value newValue = values.computeIfAbsent(value.getTimestamp(), t -> value).add(value.getValue());
                    values.put(value.getTimestamp(), newValue);
                }
            }
        }

        private Series buildSeries(String name, Map<Long, Value> values) {
            return Series.create(name, values.values());
        }

        private HeatMap buildHeatMap(String name, Map<String, Map<Long, Value>> values) {
            List<Series> series = new ArrayList<>();
            series.sort(Comparator.comparing(Series::getName));
            for (Map.Entry<String, Map<Long, Value>> entry : values.entrySet()) {
                series.add(buildSeries(entry.getKey(), entry.getValue()));
            }
            return HeatMap.create(name, series, maximumLanes);
        }

        Collection<HeatMap> build() {
            for (Matrix matrix : matrices) {
                append(matrix);
            }
            Collection<HeatMap> heatMaps = new ArrayList<>();
            for (Map.Entry<String, Map<String, Map<Long, Value>>> entry : series.entrySet()) {
                heatMaps.add(buildHeatMap(entry.getKey(), entry.getValue()));
            }
            return heatMaps;
        }
    }

}
