package net.microfalx.bootstrap.web.chart.provider;

import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.annotation.Asynchronous;

/**
 * An interface invoked during {@link Chart#toJson()}} to update the chart's series or other attributes.
 * <p>
 * If the data is changed, {@link Chart#reset()} should be called before.
 */
@Asynchronous
public interface ChartProvider {

    /**
     * Returns whether the provider is invoked asynchronously.
     *
     * @return {@code true} to be invoked asynchronously, {@code false} otherwise
     */
    default boolean isAsynchronous() {
        Asynchronous asynchronousAnnot = AnnotationUtils.getAnnotation(getClass(), Asynchronous.class);
        return asynchronousAnnot == null || asynchronousAnnot.value();
    }

    /**
     * Invoked to update the chart.
     *
     * @param chart the chart to be updated
     */
    void onUpdate(Chart chart);
}
