package net.microfalx.bootstrap.web.chart;

/**
 * An interface invoked during {@link Chart#toJson()}} to update the chart's series or other attributes.
 * <p>
 * If the data is change, {@link Chart#reset()} should be called before.
 */
public interface ChartProvider {

    /**
     * Invoked to update the chart.
     *
     * @param chart the chart to be updated
     */
    void onUpdate(Chart chart);
}
