package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.component.panel.Row;
import net.microfalx.bootstrap.web.dashboard.AbstractReportProvider;
import net.microfalx.bootstrap.web.dashboard.DashboardController;
import net.microfalx.bootstrap.web.dashboard.annotation.Dashboard;
import net.microfalx.lang.annotation.Provider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@RequestMapping("/admin/search")
@Dashboard("search-engine")
@Controller
public class SearchEngineController extends DashboardController {

    @Dashboard("search-engine")
    @Provider
    public static class Report extends AbstractReportProvider<Row> {

        private SearchEngineReportStats stats;

        @Override
        public void initialize(Object... context) {
            stats = new SearchEngineReportStats(getService(IndexService.class), getService(SearchService.class), SearchEngineReportStats.TOP_100);
        }

        private Row indexStatistics() {
            Row row = Row.create();
            row.add(stats.getIndexStatistics());
            stats.buildFieldStatistics(row);
            return row;
        }

        private Row fieldStatistics() {
            Row row = Row.create();
            row.add(stats.getFieldStatistics());
            row.add(stats.termsStatistics());
            return row;
        }

        @Override
        public Iterable<Row> get() {
            return Arrays.asList(indexStatistics(), fieldStatistics());
        }
    }
}
