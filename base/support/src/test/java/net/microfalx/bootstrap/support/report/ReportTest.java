package net.microfalx.bootstrap.support.report;

import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ReportTest extends AbstractReportServiceTestCase {

    @Test
    void defaultSettings() throws IOException {
        Report report = reportService.createReport();
        Resource resource = Resource.memory();
        report.render(resource);
        Assertions.assertThat(resource.loadAsString()).contains("btn-back-to-top")
                .contains("class=\"page\"").contains("aaa");
    }

}