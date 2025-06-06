package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.web.chart.annotations.Annotations;
import net.microfalx.bootstrap.web.chart.datalabels.DataLabels;
import net.microfalx.bootstrap.web.chart.grid.Grid;
import net.microfalx.bootstrap.web.chart.legend.Legend;
import net.microfalx.bootstrap.web.chart.markers.Markers;
import net.microfalx.bootstrap.web.chart.nodata.NoData;
import net.microfalx.bootstrap.web.chart.plot.PlotOptions;
import net.microfalx.bootstrap.web.chart.provider.ChartProvider;
import net.microfalx.bootstrap.web.chart.series.Series;
import net.microfalx.bootstrap.web.chart.series.Value;
import net.microfalx.bootstrap.web.chart.states.States;
import net.microfalx.bootstrap.web.chart.style.Fill;
import net.microfalx.bootstrap.web.chart.style.Stroke;
import net.microfalx.bootstrap.web.chart.theme.Theme;
import net.microfalx.bootstrap.web.chart.title.Title;
import net.microfalx.bootstrap.web.chart.tooltip.Tooltip;
import net.microfalx.bootstrap.web.chart.xaxis.XAxis;
import net.microfalx.bootstrap.web.chart.yaxis.YAxis;
import net.microfalx.lang.*;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Instant;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A chart definition for <a href="https://apexcharts.com/">ApexCharts</a>
 */
@Data
@ToString
public class Chart implements Identifiable<String>, Nameable, Descriptable {

    @JsonIgnore
    private String id;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String description;

    @JsonProperty("chart")
    private final Options options;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Annotations annotations;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object[] colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DataLabels dataLabels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Fill fill;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Grid grid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Legend legend;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Markers markers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private NoData noData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlotOptions plotOptions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Responsive[] responsive;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private States states;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Stroke stroke;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Title subtitle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Theme theme;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private XAxis xaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private YAxis[] yaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object series;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] labels;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Tooltip tooltip;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean debug;

    @JsonIgnore
    private ChartProvider provider;
    @JsonIgnore
    private Attributes<?> attributes = Attributes.create();

    public static Chart create(Type type) {
        requireNonNull(type);
        return create(Options.create(type));
    }

    public static Chart create(Options options) {
        return new Chart(options);
    }

    Chart(Options options) {
        requireNonNull(options);
        this.options = options;
        this.id = "chart_" + IdGenerator.get("chart").nextAsString();
    }

    /**
     * Changes the chart identifier.
     *
     * @param id the identifier
     */
    public void setId(String id) {
        requireNotEmpty(id);
        this.id = id;
    }

    /**
     * Adds a new series to the chart.
     *
     * @param series the series
     * @param <T>    the series type
     * @return self
     */
    public <T> Chart addSeries(Series<T> series) {
        requireNonNull(series);
        if (this.series == null) this.series = new Series[0];
        this.series = ArrayUtils.add((Series[]) this.series, series);
        validateSeries();
        return this;
    }

    /**
     * Adds a new metrics series to the chart.
     *
     * @param series the series
     * @return self
     */
    public <T> Chart addSeries(net.microfalx.metrics.Series series) {
        requireNonNull(series);
        List<Value<Instant, Float>> values = series.getValues().stream().map(value -> Value.create(value.atInstant(), value.getValue())).toList();
        return addSeries(Series.create(series.getName(), values));
    }

    /**
     * Fills in some default labels based on registered series.
     */
    public Chart addDefaultLabels() {
        if (labels == null && ObjectUtils.isNotEmpty(series)) {
            Collection<String> newLabels = new ArrayList<>();
            int elementCount = ObjectUtils.getArrayLength(((Series[]) series)[0].getData());
            for (int i = 1; i <= elementCount; i++) {
                newLabels.add(Integer.toString(i));
            }
            this.labels = newLabels.toArray(new String[0]);
        }
        return this;
    }

    /**
     * Sets a list of colors to be used to plot the chart.
     *
     * @param colors an array of colors.
     * @return self
     */
    public Chart setColors(Object... colors) {
        this.colors = colors;
        return this;
    }

    /**
     * Sets a list of labels to be used to with values in the series.
     *
     * @param labels the labels
     * @return a non-null instance
     */
    public Chart setLabels(String... labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets a list of labels to be used to with values in the series.
     *
     * @param labels the labels
     * @return a non-null instance
     */
    public Chart setLabels(Iterable<String> labels) {
        this.labels = Iterables.toArray(labels, String.class);
        return this;
    }

    /**
     * Generates the JSON which describes the chart.
     *
     * @return a non-empty String
     */
    public String toJson() {
        update();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Map<String, ?> state = saveState();
        try {
            updateProperties();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ChartException("Failed to create the JSON for chart " + this, e);
        } finally {
            restoreState(state);
        }
    }

    /**
     * Resets the data associated with the cart.
     */
    public void reset() {
        this.series = new Series<?>[0];
        this.labels = new String[0];
        this.xaxis = null;
        this.yaxis = null;
    }

    /**
     * Returns the attributes associated with the chart.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes() {
        return attributes;
    }

    private void update() {
        if (provider != null) provider.onUpdate(this);
    }

    private Map<String, ?> saveState() {
        Map<String, Object> map = new HashMap<>();
        map.put("series", this.series);
        return map;
    }

    private void updateProperties() {
        Series<?>[] series = (Series<?>[]) this.series;
        if (series != null && series.length == 1 && StringUtils.isEmpty(series[0].getName())) {
            this.series = series[0].getData().toArray();
        }
    }

    private void restoreState(Map<String, ?> state) {
        this.series = state.get("series");
    }

    private void validateSeries() {
        int prevElementCount = -1;
        Series<?> prevSeries;
        for (Series<?> currentSeries : (Series[]) series) {
            prevSeries = currentSeries;
            int currentElementCount = ObjectUtils.getArrayLength(currentSeries.getData());
            if (prevElementCount == -1) {
                prevElementCount = currentElementCount;
            } else {
                if (prevElementCount != currentElementCount) {
                    throw new ChartException("Series '" + currentSeries.getName() + "' has a different number of elements ("
                            + currentElementCount + ") compared with series '"
                            + prevSeries.getName() + "' (" + prevElementCount + ")");
                }
            }

        }
    }
}


