package net.microfalx.bootstrap.metrics;

import lombok.ToString;
import net.microfalx.lang.Nameable;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A heatmap.
 */
@ToString
public final class HeatMap implements Nameable {

    private final String name;
    private final List<Series> series;

    /**
     * Creates an empty series for a given metric and its values.
     *
     * @param name the name of the series
     * @return a non-null instance
     */
    public static HeatMap create(String name) {
        return new HeatMap(name, null);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param series the iterable
     * @return a non-null instance
     */
    public static HeatMap create(String name, Iterable<Series> series) {
        return new HeatMap(name, series);
    }

    HeatMap(String name, Iterable<Series> series) {
        requireNonNull(name);
        this.name = name;
        this.series = toList(series);
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

}
