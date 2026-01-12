package net.microfalx.bootstrap.web.controller.support;

import net.microfalx.bootstrap.support.report.Report;
import net.microfalx.bootstrap.support.report.ReportService;
import net.microfalx.bootstrap.web.controller.AnonymousController;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/support/report")
public class ReportController implements AnonymousController {

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
}
