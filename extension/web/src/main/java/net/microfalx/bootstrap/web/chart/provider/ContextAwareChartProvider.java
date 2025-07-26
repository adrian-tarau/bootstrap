package net.microfalx.bootstrap.web.chart.provider;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;

/**
 * A chart provider which is aware of the Spring context.
 */
public abstract class ContextAwareChartProvider extends ApplicationContextSupport implements ChartProvider {
}
