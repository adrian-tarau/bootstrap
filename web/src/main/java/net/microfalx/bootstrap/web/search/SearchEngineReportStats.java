package net.microfalx.bootstrap.web.search;

import com.google.common.collect.Iterables;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.search.*;
import net.microfalx.bootstrap.web.component.Link;
import net.microfalx.bootstrap.web.component.panel.Column;
import net.microfalx.bootstrap.web.component.panel.Row;
import net.microfalx.bootstrap.web.component.panel.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static net.microfalx.bootstrap.model.AttributeConstants.MAX_ATTRIBUTE_DISPLAY_LENGTH;
import static net.microfalx.bootstrap.model.AttributeUtils.isSingleLineAndShort;
import static net.microfalx.bootstrap.search.SearchUtils.isStandardFieldName;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * Central point of extraction for all stats.
 */
class SearchEngineReportStats {

    public static final int TOP_100 = 100;
    public static final int TOP_10 = 10;

    private final IndexService indexService;
    private final SearchService searchService;

    private int limit;

    SearchEngineReportStats(IndexService indexService, SearchService searchService, int limit) {
        requireNonNull(indexService);
        requireNonNull(searchService);
        this.indexService = indexService;
        this.searchService = searchService;
        this.limit = limit;
    }

    Column getIndexStatistics() {
        Collection<FieldStatistics> fieldStatistics = searchService.getFieldStatistics();
        return Column.create(3).setTitle("Index")
                .add(Table.create(2)
                        .addRow("Total Documents", indexService.getDocumentCount())
                        .addRow("Pending Documents", indexService.getPendingDocumentCount())
                        .addRow("Total Fields", fieldStatistics.size())
                );
    }

    Column getFieldStatistics() {
        Iterable<FieldStatistics> fields = Iterables.limit(searchService.getFieldStatistics().stream()
                .filter(f -> !isStandardFieldName(f.getName())).collect(Collectors.toList()), TOP_100);
        return Column.create(5).setTitle("Top 100 Fields")
                .add(Table.create("Name", "Documents").setMaxHeight("300")
                        .addRows(table -> fields.forEach(fs -> table.addRow(abbreviate(fs.getName(), MAX_ATTRIBUTE_DISPLAY_LENGTH),
                                fs.getDocumentCount())))
                );
    }

    void buildFieldStatistics(Row row) {
        row.add(getSeverityStatistics());
        row.add(getOwnerStatistics());
        row.add(getTypeStatistics());
        row.add(getSourceStatistics());
        row.add(getTargetStatistics());
        row.add(getBodyStatistics());
    }

    Column getSeverityStatistics() {
        return getFieldStatistics("Top {limit} Severities", Attribute.SEVERITY);
    }

    Column getSourceStatistics() {
        return getFieldStatistics("Top {limit} Sources", Document.SOURCE_FIELD);
    }

    Column getTargetStatistics() {
        return getFieldStatistics("Top {limit} Targets", Document.TARGET_FIELD);
    }

    Column getOwnerStatistics() {
        return getFieldStatistics("Top {limit} Owners", Document.OWNER_FIELD);
    }

    Column getTypeStatistics() {
        return getFieldStatistics("Top {limit} Types", Document.TYPE_FIELD);
    }
    Column getBodyStatistics() {
        return getFieldStatistics("Top {limit} Tokens", Document.BODY_FIELD);
    }

    Column termsStatistics() {
        PriorityQueue<TermStatistics> priorityQueue = new PriorityQueue<>(comparingLong(TermStatistics::getCount).reversed());
        for (FieldStatistics fieldStatistic : searchService.getFieldStatistics()) {
            if (isStandardFieldName(fieldStatistic.getName())) continue;
            for (TermStatistics term : fieldStatistic.getTerms()) {
                if (!isSingleLineAndShort(term.getName())) continue;
                priorityQueue.add(term);
            }
        }
        Iterable<TermStatistics> terms = Iterables.limit(priorityQueue, TOP_100);
        return Column.create(7).setTitle("Top 100 Terms")
                .add(Table.create("Name", "Field", "Documents", "Frequency").setMaxHeight("300")
                        .addRows(table -> terms.forEach(term -> table.addRow(abbreviate(term.getName(), MAX_ATTRIBUTE_DISPLAY_LENGTH), term.getField(),
                                term.getCount(), term.getFrequency())))
                );
    }

    private Column getFieldStatistics(String title, String fieldName) {
        title = StringUtils.replaceOnce(title, "{limit}", Integer.toString(limit));
        FieldStatistics fieldStatistics = searchService.getFieldStatistics(fieldName);
        List<TermStatistics> terms = fieldStatistics.getTerms();
        return Column.create(3).setTitle(title).add(createTermsTable(fieldName, terms));
    }

    private Table createTermsTable(String fieldName, List<TermStatistics> terms) {
        Iterable<TermStatistics> limitedTerms = Iterables.limit(terms, limit);
        return Table.create("Value", "Count").setMaxHeight("300")
                .addRows(table -> limitedTerms.forEach(term -> table.addRow(termLink(fieldName, abbreviate(term.getName(), MAX_ATTRIBUTE_DISPLAY_LENGTH)),
                        term.getCount())));
    }

    private Link termLink(String fieldName, String term) {
        return Link.action(term, "search").addParameter("query", fieldName + ": \"" + term+"\"");
    }
}