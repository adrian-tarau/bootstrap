package net.microfalx.bootstrap.web.chart;

/**
 * An interface invoked during {@link Chart#toJson()}} to update the chart's series or other attributes.
 * <p>
 * If the data is change, {@link Chart#reset()} should be called before.
 */
public interface ChartProvider {

    /**
     * Returns whether the provider is invoked asynchronously.
     *
     * @return {@code true} to be invoked asynchronously, {@code false} otherwise
     */
    default boolean isAsynchronous() {
        return true;
    }

    /**
     * Invoked to update the chart.
     *
     * @param chart the chart to be updated
     */
    void onUpdate(Chart chart);
}
