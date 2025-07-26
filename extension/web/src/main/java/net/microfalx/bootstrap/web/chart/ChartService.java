package net.microfalx.bootstrap.web.chart;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which tracks charts and integrates them in web pages.
 */
@Service
public class ChartService {

    private final Map<String, Chart> charts = new ConcurrentHashMap<>();

    /**
     * Returns a previously registered chart.
     *
     * @param id the char identifier
     * @return a non-null instance
     */
    public Chart get(String id) {
        requireNonNull(id);
        Chart chart = charts.get(id.toLowerCase());
        if (chart == null) throw new ChartNotFoundException("A chart with identifier '" + id + "'is not registered");
        return chart;
    }

    /**
     * Registers a chart for later use.
     *
     * @param chart the chart
     */
    public void register(Chart chart) {
        requireNonNull(chart);
        charts.put(chart.getId().toLowerCase(), chart);
    }
}
