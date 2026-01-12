package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.logger.AlertEvent;
import net.microfalx.bootstrap.logger.LoggerService;
import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Report;
import net.microfalx.bootstrap.support.report.Template;

import java.util.Collection;

@net.microfalx.lang.annotation.Provider
public class LoggerProvider extends AbstractFragmentProvider {

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
        return getLoggerService().getAlerts(report.getStartTime().toLocalDateTime(),
                report.getEndTime().toLocalDateTime());
    }


    private LoggerService getLoggerService() {
        return getBean(LoggerService.class);
    }
}
