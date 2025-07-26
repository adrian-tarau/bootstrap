package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.component.panel.Row;
import net.microfalx.bootstrap.web.dashboard.AbstractReportProvider;
import net.microfalx.bootstrap.web.dashboard.annotation.Dashboard;
import net.microfalx.lang.annotation.Provider;

import java.util.Arrays;

@Dashboard("home")
@Provider
public class HomePageSearchEngineReport extends AbstractReportProvider<Row> {

    @Override
    public Iterable<Row> get() {
        SearchEngineReportStats stats = new SearchEngineReportStats(getService(IndexService.class), getService(SearchService.class),
                SearchEngineReportStats.TOP_10);
        Row row = Row.create("Search Engine").setId("search").setPosition(1);
        stats.buildFieldStatistics(row);
        return Arrays.asList(row);
    }
}
