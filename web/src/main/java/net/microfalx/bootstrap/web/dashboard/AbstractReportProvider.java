package net.microfalx.bootstrap.web.dashboard;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.lang.Initializable;

/**
 * Base class for all report providers.
 *
 * @param <C> the component type.
 */
public abstract class AbstractReportProvider<C extends Component<C>> implements ReportProvider<C>, Initializable {

    DashboardService dashboardService;

    /**
     * Returns the dashboard service.
     *
     * @return a non-null instance
     */
    public final DashboardService getDashboardService() {
        return dashboardService;
    }

    /**
     * Finds a service by class.
     *
     * @param serviceClass the service class
     * @param <S>          the service type
     * @return the service instance
     */
    protected final <S> S getService(Class<S> serviceClass) {
        return getDashboardService().getBean(serviceClass);
    }

    @Override
    public void initialize(Object... context) {

    }
}
