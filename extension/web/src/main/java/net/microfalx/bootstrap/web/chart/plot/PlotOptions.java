package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.boxplot.BoxPlot;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Data
@ToString
public class PlotOptions {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Bar bar;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Candlestick candlestick;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Heatmap heatmap;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Treemap treemap;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pie pie;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Radar radar;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RadialBar radialBar;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BoxPlot boxPlot;

    PlotOptions() {
    }

    public static PlotOptions bar(Bar bar) {
        requireNonNull(bar);
        return new PlotOptions().setBar(bar);
    }

    public static PlotOptions pie(Pie pie) {
        requireNonNull(pie);
        return new PlotOptions().setPie(pie);
    }

    public static PlotOptions heatMap(Heatmap heatmap) {
        requireNonNull(heatmap);
        return new PlotOptions().setHeatmap(heatmap);
    }

}
