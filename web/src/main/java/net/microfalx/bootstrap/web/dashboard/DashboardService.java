package net.microfalx.bootstrap.web.dashboard;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.bootstrap.web.dashboard.DashboardUtils.I18N_PREFIX;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;

/**
 * A service which manages a collection of dashboards and their panels.
 */
@Service
public class DashboardService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

    private Map<String, DashboardHolder> dashboards = new ConcurrentHashMap<>();

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private I18nService i18nService;

    /**
     * Returns a dashboard by its identifier.
     *
     * @param id the identifier
     * @return a non-null instance
     * @throws DashboardException if the dashboard cannot be located
     */
    public Dashboard getDashboard(String id) {
        requireNonNull(id);
        DashboardHolder holder = dashboards.get(toIdentifier(id));
        if (holder == null) throw new DashboardException("A dashboard with identifier '" + id + "' is not registered");
        return create(holder);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverDashboards();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void discoverDashboards() {
        LOGGER.info("Discover dashboards");
        Collection<ReportProvider> reportProviders = ClassUtils.resolveProviderInstances(ReportProvider.class);
        for (ReportProvider reportProvider : reportProviders) {
            net.microfalx.bootstrap.web.dashboard.annotation.Dashboard dashboardAnnot = AnnotationUtils.getAnnotation(reportProvider, net.microfalx.bootstrap.web.dashboard.annotation.Dashboard.class);
            if (dashboardAnnot == null) {
                LOGGER.error("A report provider ({}) does not have a @Dashboard annotation", ClassUtils.getName(reportProvider));
            } else {
                String id = dashboardAnnot.value();
                DashboardHolder holder = dashboards.computeIfAbsent(toIdentifier(id), s -> new DashboardHolder(id));
                holder.title = defaultIfEmpty(getI18n(holder, "title"), capitalizeWords(holder.id));
                holder.description = getI18n(holder, "description");
                holder.providers.add(reportProvider);
                if (reportProvider instanceof AbstractReportProvider<?> abstractReportProvider) {
                    abstractReportProvider.applicationContext = applicationContext;
                    abstractReportProvider.initialize();
                }
            }
        }
        LOGGER.info("Dashboards" + dashboards.size());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Dashboard create(DashboardHolder holder) {
        Dashboard dashboard = new Dashboard().setId(holder.id).setTitle(holder.title).setDescription(holder.description);
        for (ReportProvider<? extends Component<?>> provider : holder.providers) {
            for (Component<? extends Component<?>> component : provider.get()) {
                dashboard.add((Component) component);
            }
        }
        return dashboard;
    }

    private String getI18n(DashboardHolder dashboard, String suffix) {
        return i18nService.getText(I18N_PREFIX + "." + dashboard.id + "." + suffix);
    }

    static class DashboardHolder {

        private final String id;
        private String title;
        private String description;
        private Collection<ReportProvider<? extends Component<?>>> providers = new ArrayList<>();

        DashboardHolder(String id) {
            requireNonNull(id);
            this.id = toIdentifier(id);
        }

    }
}
