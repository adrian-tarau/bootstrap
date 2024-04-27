package net.microfalx.bootstrap.search;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.search.Document.STORED_SUFFIX_FIELD;
import static net.microfalx.bootstrap.search.FieldTrendCollector.getStoredTimestampField;

public class DocumentTrendCollector extends SimpleCollector {

    private static final String CREATED_AT_FIELD = net.microfalx.bootstrap.search.Document.CREATED_AT_FIELD + STORED_SUFFIX_FIELD;

    private final String timestampField;
    private final Set<String> fieldsToLoad = new HashSet<>();
    private DocumentTrend trend = new DocumentTrend();
    private LeafReaderContext context;
    private final AtomicInteger docCount = new AtomicInteger();
    private final AtomicInteger matchingDocCount = new AtomicInteger();

    DocumentTrendCollector(String timestampField) {
        this.timestampField = getStoredTimestampField(timestampField);
        this.fieldsToLoad.add(timestampField);
        this.fieldsToLoad.add(CREATED_AT_FIELD);
    }

    @Override
    public void collect(int doc) throws IOException {
        matchingDocCount.incrementAndGet();
        org.apache.lucene.document.Document document = context.reader().storedFields().document(doc, fieldsToLoad);
        long timestamp = getTimestamp(document);
        if (timestamp > 0) trend.increment(timestamp);

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

    private long getTimestamp(org.apache.lucene.document.Document document) {
        String timestamp = document.get(timestampField);
        if (timestamp == null) document.get(CREATED_AT_FIELD);
        return timestamp != null ? Long.parseLong(timestamp) : 0;
    }

    static class Manager implements CollectorManager<DocumentTrendCollector, Integer> {

        private final String timestampField;

        private DocumentTrend trend;
        private int docCount;
        private int matchingDocCount;

        public Manager(String timestampField) {
            this.timestampField = timestampField;
        }

        int getDocCount() {
            return docCount;
        }

        int getMatchingDocCount() {
            return matchingDocCount;
        }

        DocumentTrend getTrend() {
            return trend;
        }

        @Override
        public DocumentTrendCollector newCollector() throws IOException {
            return new DocumentTrendCollector(timestampField);
        }

        @Override
        public Integer reduce(Collection<DocumentTrendCollector> collectors) throws IOException {
            trend = new DocumentTrend();
            for (DocumentTrendCollector collector : collectors) {
                matchingDocCount += collector.matchingDocCount.get();
                docCount += collector.docCount.get();
                trend.merge(collector.trend);
            }
            return this.trend.getCounts().size();
        }
    }
}
