package net.microfalx.bootstrap.web.dashboard;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.lang.Initializable;
import org.springframework.context.ApplicationContext;

/**
 * Base class for all report providers
 *
 * @param <C>
 */
public abstract class AbstractReportProvider<C extends Component<C>> implements ReportProvider<C>, Initializable {

    ApplicationContext applicationContext;

    /**
     * Finds a service by class.
     *
     * @param serviceClass the service class
     * @param <S>          the service type
     * @return the service instance
     */
    protected final <S> S getService(Class<S> serviceClass) {
        return applicationContext.getBean(serviceClass);
    }

    @Override
    public void initialize(Object... context) {

    }
}
