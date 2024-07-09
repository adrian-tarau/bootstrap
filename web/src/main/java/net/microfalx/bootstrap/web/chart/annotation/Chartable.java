package net.microfalx.bootstrap.web.chart.annotation;

import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.provider.ChartProvider;

import java.lang.annotation.*;

/**
 * An annotation used to provide charting capabilities to annotated element.
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Chartable {

    /**
     * Returns whether the summary of the annotated element should be displayed.
     * <p>
     * In most cases we only want to show the chart and the summary is disabled by default.
     *
     * @return {@code true} to
     */
    boolean displaySummary() default false;

    /**
     * Returns the width of the chart.
     *
     * @return the width of the chart, -1 if there is no fixed width
     */
    int width() default -1;

    /**
     * Returns the height of the chart.
     *
     * @return the height of the chart, -1 if there is no fixed height
     */
    int height() default -1;

    /**
     * Returns the type of chart.
     *
     * @return a non-null instance
     */
    Type type() default Type.BAR;

    /**
     * Returns the provider which updates the chart.
     *
     * @return a non-null class
     */
    Class<? extends ChartProvider> provider();
}
