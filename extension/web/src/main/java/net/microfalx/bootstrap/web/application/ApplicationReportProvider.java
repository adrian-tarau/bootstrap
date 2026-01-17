package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.lang.annotation.Provider;

import static net.microfalx.bootstrap.support.report.Template.APPLICATION_VARIABLE;

@Provider
public class ApplicationReportProvider extends AbstractFragmentProvider {

    @Override
    public Fragment create() {
        return Fragment.builder("Application").template("application")
                .visible(false).icon("fa-solid fa-application")
                .build();
    }

    @Override
    public void update(Template template) {
        ApplicationService applicationService = getBean(ApplicationService.class);
        template.addVariable(APPLICATION_VARIABLE, applicationService.getApplication());
    }
}
