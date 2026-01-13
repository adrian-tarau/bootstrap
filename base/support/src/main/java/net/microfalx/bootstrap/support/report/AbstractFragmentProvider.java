package net.microfalx.bootstrap.support.report;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.metrics.Metric;
import net.microfalx.metrics.Series;
import net.microfalx.metrics.SeriesStore;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all fragment providers.
 */
public abstract class AbstractFragmentProvider extends ApplicationContextSupport implements Fragment.Provider {

    private ReportService reportService;

    /**
     * Returns a provider by its class.
     *
     * @param providerClass the provider
     * @param <P>           the provider type
     * @return the provider
     */
    protected final <P extends Fragment.Provider> P getProvider(Class<P> providerClass) {
        requireNonNull(providerClass);
        if (reportService == null) {
            reportService = getBean(ReportService.class);
        }
        return reportService.getProviderByClass(providerClass);
    }

    /**
     * Returns the series for the given metric in the current report time interval.
     *
     * @param store  the store
     * @param metric the metrics
     * @return a non-null instance
     */
    protected final Series getSeries(SeriesStore store, Metric metric) {
        requireNonNull(store);
        requireNonNull(metric);
        Report report = Report.current();
        return store.get(metric, report.getStartTime(), report.getEndTime());
    }
}
