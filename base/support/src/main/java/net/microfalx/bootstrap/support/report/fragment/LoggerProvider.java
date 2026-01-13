package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.logger.AlertEvent;
import net.microfalx.bootstrap.logger.LoggerService;
import net.microfalx.bootstrap.support.report.*;

import java.util.Collection;

@net.microfalx.lang.annotation.Provider
public class LoggerProvider extends AbstractFragmentProvider {

    private final TrendHelper trendHelper = new TrendHelper();

    @Override
    public Fragment create() {
        return Fragment.builder("Logger").template("logger")
                .icon("fa-regular fa-file-lines")
                .order(900)
                .build();
    }

    @Override
    public void update(Template template) {
        super.update(template);
        template.addVariable("logger", this);
    }

    /**
     * Returns the application alerts for a given time interval.
     *
     * @return a non-null instance
     */
    public Collection<AlertEvent> getAlerts() {
        Report report = Report.current();
        return report.getAttribute("alerts", this::doGetAlerts);
    }

    /**
     * Returns the application alerts for a given time interval.
     *
     * @return a non-null instance
     */
    public Collection<AlertEvent> getPendingAlerts() {
        Report report = Report.current();
        return report.getAttribute("alerts-pending", () -> doGetAlerts().stream().filter(e -> !e.isAcknowledged()).toList());
    }

    public Chart.PieChart<Integer> getAlertLevelPieChart(String id) {
        Chart.PieChart<Integer> chart = new Chart.PieChart<>(id, "Levels");
        chart.getLegend().setShow(false);
        trendHelper.aggregateInt(getPendingAlerts(), e -> e.getLevel().name(), AlertEvent::getPendingEventCount)
                .forEach(chart::add);
        return chart;
    }

    public Chart.PieChart<Integer> getAlertFailureTypePieChart(String id) {
        Chart.PieChart<Integer> chart = new Chart.PieChart<>(id, "Failure Types");
        chart.getLegend().setShow(false);
        trendHelper.aggregateInt(getPendingAlerts(), AlertEvent::getFailureType, AlertEvent::getPendingEventCount)
                .forEach(chart::add);
        return chart;
    }

    private Collection<AlertEvent> doGetAlerts() {
        Report report = Report.current();
        return getLoggerService().getAlerts(report.getStartTime().toLocalDateTime(),
                report.getEndTime().toLocalDateTime());
    }

    private LoggerService getLoggerService() {
        return getBean(LoggerService.class);
    }
}
