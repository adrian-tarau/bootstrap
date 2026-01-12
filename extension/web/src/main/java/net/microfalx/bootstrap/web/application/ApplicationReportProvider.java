package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.lang.annotation.Provider;

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
        template.addVariable("application", applicationService.getApplication());
    }
}
