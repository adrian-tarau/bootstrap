package net.microfalx.bootstrap.search;

import net.microfalx.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.search.Document.STORED_SUFFIX_FIELD;

class FieldTrendCollector extends SimpleCollector {

    private static final String CREATED_AT_FIELD = net.microfalx.bootstrap.search.Document.CREATED_AT_FIELD + STORED_SUFFIX_FIELD;

    private final String timestampField;
    private final Set<String> fields;
    private final boolean hasFields;
    private LeafReaderContext context;
    private final AtomicInteger docCount = new AtomicInteger();
    private final AtomicInteger matchingDocCount = new AtomicInteger();

    private final Map<String, FieldTrend> trends = new HashMap<>();

    FieldTrendCollector(String timestampField, Set<String> fields) {
        this.timestampField = getStoredTimestampField(timestampField);
        this.fields = new HashSet<>(fields);
        this.hasFields = !fields.isEmpty();
    }

    @Override
    public void collect(int doc) throws IOException {
        matchingDocCount.incrementAndGet();
        Document document = context.reader().storedFields().document(doc);
        long timestamp = getTimestamp(document);
        if (timestamp == 0) return;
        for (IndexableField field : document.getFields()) {
            if (hasFields && fields.contains(field.name())) {
                processDocument(document, timestamp, field);
            } else if (!(hasFields || SearchUtils.isStandardFieldName(field.name()))) {
                processDocument(document, timestamp, field);
            }
        }
    }

    private void processDocument(Document document, long timestamp, IndexableField field) {
        FieldTrend fieldTrend = trends.computeIfAbsent(field.name(), FieldTrend::new);
        String value = document.get(field.name());
        if (value != null) fieldTrend.increment(timestamp, value);
    }

    private long getTimestamp(Document document) {
        String timestamp = document.get(timestampField);
        if (timestamp == null) document.get(CREATED_AT_FIELD);
        return timestamp != null ? Long.parseLong(timestamp) : 0;
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) throws IOException {
        this.context = context;
        this.docCount.addAndGet(context.reader().numDocs());
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    static String getStoredTimestampField(String name) {
        if (StringUtils.isEmpty(name)) name = net.microfalx.bootstrap.search.Document.CREATED_AT_FIELD;
        return name + STORED_SUFFIX_FIELD;
    }

    static class Manager implements CollectorManager<FieldTrendCollector, Integer> {

        private final String timestampField;
        private final Set<String> fields;

        private Collection<FieldTrend> trends = Collections.emptyList();
        private int docCount;
        private int matchingDocCount;

        public Manager(String timestampField, Set<String> fields) {
            this.timestampField = timestampField;
            this.fields = new HashSet<>(fields);
        }

        int getDocCount() {
            return docCount;
        }

        int getMatchingDocCount() {
            return matchingDocCount;
        }

        Collection<FieldTrend> getTrends() {
            return trends;
        }

        @Override
        public FieldTrendCollector newCollector() throws IOException {
            return new FieldTrendCollector(timestampField, fields);
        }

        @Override
        public Integer reduce(Collection<FieldTrendCollector> collectors) throws IOException {
            Map<String, FieldTrend> trends = new HashMap<>();
            for (FieldTrendCollector collector : collectors) {
                matchingDocCount += collector.matchingDocCount.get();
                docCount += collector.docCount.get();
                for (Map.Entry<String, FieldTrend> entry : collector.trends.entrySet()) {
                    trends.merge(entry.getKey(), entry.getValue(), (trendOld, trendNew) -> {
                        trendOld.merge(trendNew);
                        return trendOld;
                    });
                }
            }
            this.trends = trends.values();
            return this.trends.size();
        }
    }
}
