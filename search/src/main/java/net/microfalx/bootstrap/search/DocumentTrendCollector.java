package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.metrics.Aggregation;
import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.search.Document.STORED_SUFFIX_FIELD;
import static net.microfalx.bootstrap.search.FieldTrendCollector.getStoredTimestampField;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class DocumentTrendCollector extends SimpleCollector {

    private static final String CREATED_AT_FIELD = net.microfalx.bootstrap.search.Document.CREATED_AT_FIELD + STORED_SUFFIX_FIELD;
    private static final Metric metric = Metric.create("document.trend");

    private final String timestampField;
    private final Set<String> fieldsToLoad = new HashSet<>();
    private final Aggregation aggregation = new Aggregation();
    private LeafReaderContext context;
    private final AtomicInteger docCount = new AtomicInteger();
    private final AtomicInteger matchingDocCount = new AtomicInteger();

    DocumentTrendCollector(String timestampField, Duration step) {
        this.timestampField = getStoredTimestampField(timestampField);
        this.fieldsToLoad.add(this.timestampField);
        this.fieldsToLoad.add(CREATED_AT_FIELD);
        this.aggregation.setStep(step);
    }

    @Override
    public void collect(int doc) throws IOException {
        matchingDocCount.incrementAndGet();
        org.apache.lucene.document.Document document = context.reader().storedFields().document(doc, fieldsToLoad);
        long timestamp = getTimestamp(document);
        if (timestamp > 0) aggregation.add(metric, Value.create(timestamp, 1));
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

        private final Duration step;
        private final String timestampField;

        private Matrix trend;
        private int docCount;
        private int matchingDocCount;

        public Manager(String timestampField, Duration step) {
            requireNonNull(timestampField);
            requireNonNull(step);
            this.timestampField = timestampField;
            this.step = step;
        }

        int getDocCount() {
            return docCount;
        }

        int getMatchingDocCount() {
            return matchingDocCount;
        }

        Matrix getTrend() {
            return trend;
        }

        @Override
        public DocumentTrendCollector newCollector() throws IOException {
            return new DocumentTrendCollector(timestampField, step);
        }

        @Override
        public Integer reduce(Collection<DocumentTrendCollector> collectors) throws IOException {
            Aggregation aggregation = new Aggregation().setStep(step);
            for (DocumentTrendCollector collector : collectors) {
                matchingDocCount += collector.matchingDocCount.get();
                docCount += collector.docCount.get();
                aggregation.merge(collector.aggregation);
            }
            Collection<Matrix> matrixes = aggregation.toMatrixes();
            trend = matrixes.isEmpty() ? Matrix.create(metric, Collections.emptyList()) : matrixes.iterator().next();
            return trend.getValues().size();
        }
    }
}
