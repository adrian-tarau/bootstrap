package net.microfalx.bootstrap.web.dashboard;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.panel.Row;

/**
 * An interface which provides reports for dashboards. A report can be any component but most providers
 * will provide {@link Row rows} within a dashboard.
 * <p>
 * Each provider needs to be annotated with {@link net.microfalx.bootstrap.web.dashboard.annotation.Dashboard} annotation
 * which provides which dashboard will host the report.
 */
public interface ReportProvider<C extends Component<C>> {

    /**
     * Returns components to be added to a dashboard.
     *
     * @return a non-null instance
     */
    Iterable<C> get();

}
