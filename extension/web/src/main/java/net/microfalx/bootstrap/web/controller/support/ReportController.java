package net.microfalx.bootstrap.web.controller.support;

import jakarta.annotation.security.RolesAllowed;
import net.microfalx.bootstrap.support.report.Report;
import net.microfalx.bootstrap.support.report.ReportService;
import net.microfalx.bootstrap.web.application.annotation.SystemTheme;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/support/report")
@SystemTheme
@RolesAllowed("admin")
public class ReportController extends PageController {

    @Autowired private ReportService reportService;

    @GetMapping("")
    @ResponseBody
    public String home() {
        Report report = reportService.createReport();
        Resource reportBody = Resource.temporary("report", "html");
        try {
            report.render(reportBody);
            return reportBody.loadAsString();
        } catch (Exception e) {
            return "Error generating report: " + ExceptionUtils.getRootCauseMessage(e);
        }
    }

    @GetMapping("{id}")
    @ResponseBody
    public String fragment(@PathVariable("id") String id, @RequestParam(value = "dynamic", defaultValue = "true") boolean dynamic) {
        Report report = reportService.createReport();
        report.setFragment(id).setDynamic(dynamic);
        Resource reportBody = Resource.temporary("report_export", ".html");
        try {
            report.render(reportBody);
            return reportBody.loadAsString();
        } catch (Exception e) {
            return "Error generating report: " + ExceptionUtils.getRootCauseMessage(e);
        }
    }
}
