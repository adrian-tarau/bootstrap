package net.microfalx.bootstrap.support.report;

import net.microfalx.lang.Nameable;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.TimeUtils;
import net.microfalx.metrics.Value;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

public class Chart extends NamedIdentityAware<String> {

    private static final long offsetMillis = ZonedDateTime.now().getOffset().getTotalSeconds() * TimeUtils.MILLISECONDS_IN_SECOND;

    private Integer width;
    private Integer height;
    private Legend legend = new Legend();
    private Axis xaxis = new Axis();
    private Axis yaxis = new Axis();

    public Chart(String id, String name) {
        setId(id);
        setName(name);
    }

    public Integer getWidth() {
        return width;
    }

    public Chart setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public Chart setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Legend getLegend() {
        return legend;
    }

    public Chart setLegend(Legend legend) {
        this.legend = legend;
        return this;
    }

    public Axis getXaxis() {
        return xaxis;
    }

    public Chart setXaxis(Axis xaxis) {
        this.xaxis = xaxis;
        return this;
    }

    public Axis getYaxis() {
        return yaxis;
    }

    public Chart setYaxis(Axis yaxis) {
        this.yaxis = yaxis;
        return this;
    }

    public static Series<Long, Float> convert(String name, net.microfalx.metrics.Series metricsSeries) {
        Series<Long, Float> series = new Series<>(name);
        for (Value value : metricsSeries.getValues()) {
            series.add(toMillisLocalZone(value.getTimestamp()), round(value.asFloat()));
        }
        return series;
    }

    public static <T> Series<Long, Float> convert(String name, Iterable<T> items, Function<T, Long> timestampFunction,
                                                   Function<T, Float> valueFunction) {
        Series<Long, Float> series = new Series<>(name);
        for (T item : items) {
            series.add(toMillisLocalZone(timestampFunction.apply(item)), round(valueFunction.apply(item)));
        }
        return series;
    }

    public static long toMillisLocalZone(long millis) {
        return millis + offsetMillis;
    }

    public static <V extends Number> String toString(V value) {
        if (value == null) {
            return "0";
        } else {
            return formatNumber(value, 2, EMPTY_STRING);
        }
    }

    public static float round(float value) {
        if (value < 0.001) {
            return 0;
        } else {
            return value;
        }
    }

    public enum Unit {
        COUNT,
        DATE_TIME,
        DURATION,
        BYTE,
        PERCENT
    }

    public static class Data<X, Y extends Number> {

        private X x;
        private Y y;

        public Data(X x, Y y) {
            requireNonNull(x);
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Data.class.getSimpleName() + "[", "]")
                    .add("x=" + x)
                    .add("y=" + y)
                    .toString();
        }
    }

    public static class Series<X, Y extends Number> implements Nameable {

        private final String name;
        private final Collection<Data<X, Y>> data = new ArrayList<>();

        public Series(String name) {
            requireNonNull(name);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public Collection<Data<X, Y>> getData() {
            return data;
        }

        public Series<X, Y> add(Data<X, Y> data) {
            this.data.add(data);
            return this;
        }

        public Series<X, Y> add(X x, Y y) {
            requireNonNull(x);
            requireNonNull(y);
            add(new Data<>(x, y));
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Series.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("data=" + data)
                    .toString();
        }
    }

    public static class Legend {

        private boolean show = true;

        public boolean isShow() {
            return show;
        }

        public Legend setShow(boolean show) {
            this.show = show;
            return this;
        }
    }

    public static class Axis {

        private Unit unit = Unit.COUNT;

        public Unit getUnit() {
            return unit;
        }

        public Axis setUnit(Unit unit) {
            requireNonNull(unit);
            this.unit = unit;
            return this;
        }
    }

    public static abstract class DataChart<X, Y extends Number> extends Chart {

        private final Collection<Data<X, Y>> data = new ArrayList<>();

        public DataChart(String id, String name) {
            super(id, name);
        }

        public Collection<Data<X, Y>> getData() {
            List<Data<X, Y>> sorted = new ArrayList<>(data);
            sorted.sort((o1, o2) -> -Double.compare(o1.getY().doubleValue(), o2.getY().doubleValue()));
            return unmodifiableCollection(sorted);
        }

        public DataChart<X, Y> add(X x, Y y) {
            requireNonNull(x);
            data.add(new Data<>(x, y));
            return this;
        }
    }

    public static abstract class MultiSeriesChart<X, Y extends Number> extends Chart {

        private final List<Series<X, Y>> series = new ArrayList<>();

        public MultiSeriesChart(String id, String name) {
            super(id, name);
        }

        public List<Series<X, Y>> getSeries() {
            return unmodifiableList(series);
        }

        public String getXDataType() {
            for (Series<X, Y> s : series) {
                for (Data<X, Y> data : s.getData()) {
                    X x = data.getX();
                    if (x instanceof Temporal || x instanceof Long) {
                        return "datetime";
                    }
                }
            }
            return "datetime";
        }

        public MultiSeriesChart<X, Y> add(Series<X, Y> series) {
            requireNonNull(series);
            this.series.add(series);
            return this;
        }
    }

    public static abstract class SingleSeriesChart<N extends Number> extends Chart {

        private final List<N> series = new ArrayList<>();
        private final List<String> labels = new ArrayList<>();
        private String seriesName = "Value";

        public SingleSeriesChart(String id, String name) {
            super(id, name);
        }

        public List<N> getSeries() {
            return unmodifiableList(series);
        }

        public List<String> getLabels() {
            return unmodifiableList(labels);
        }

        public String getSeriesName() {
            return seriesName;
        }

        public SingleSeriesChart<N> setSeriesName(String seriesName) {
            requireNonNull(seriesName);
            this.seriesName = seriesName;
            return this;
        }

        public SingleSeriesChart<N> add(String label, N value) {
            requireNonNull(label);
            requireNonNull(value);
            labels.add(label);
            series.add(value);
            return this;
        }

        public SingleSeriesChart<N> addLabels(Iterable<String> labels) {
            requireNonNull(labels);
            labels.forEach(this.labels::add);
            return this;
        }

        public SingleSeriesChart<N> addValues(Iterable<N> values) {
            requireNonNull(labels);
            values.forEach(series::add);
            return this;
        }
    }

    public static class PieChart<N extends Number> extends SingleSeriesChart<N> {

        public PieChart(String id, String name) {
            super(id, name);
        }
    }

    public static class BarChart<N extends Number> extends SingleSeriesChart<N> {

        public BarChart(String id, String name) {
            super(id, name);
        }
    }

    public static class ColumnChart<N extends Number> extends SingleSeriesChart<N> {

        public ColumnChart(String id, String name) {
            super(id, name);
        }
    }

    public static class AreaChart<X, Y extends Number> extends MultiSeriesChart<X, Y> {

        private boolean stacked;

        public AreaChart(String id, String name) {
            super(id, name);
        }

        public boolean isStacked() {
            return stacked;
        }

        public AreaChart<X, Y> setStacked(boolean stacked) {
            this.stacked = stacked;
            return this;
        }
    }

    public static class TreeMapChart<N extends Number> extends DataChart<String, N> {

        public TreeMapChart(String id, String name) {
            super(id, name);
        }
    }
}
