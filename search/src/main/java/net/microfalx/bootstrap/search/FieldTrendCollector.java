package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.metrics.Aggregation;
import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFields;
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
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class FieldTrendCollector extends SimpleCollector {

    private static final String CREATED_AT_FIELD = net.microfalx.bootstrap.search.Document.CREATED_AT_FIELD + STORED_SUFFIX_FIELD;
    private static final String METRIC_NAME = "field.trend";
    private static final Metric metric = Metric.create();

    private final String timestampField;
    private final Set<String> fields;
    private final Set<String> allFields;
    private final boolean hasFields;
    private LeafReaderContext context;
    private final AtomicInteger docCount = new AtomicInteger();
    private final AtomicInteger matchingDocCount = new AtomicInteger();

    private final Aggregation aggregation = new Aggregation();

    FieldTrendCollector(String timestampField, Set<String> fields, Duration step) {
        this.timestampField = getStoredTimestampField(timestampField);
        this.fields = new HashSet<>(fields);
        this.allFields = new HashSet<>(this.fields);
        this.allFields.add(this.timestampField);
        this.allFields.add(DocumentTrendCollector.CREATED_AT_FIELD);
        this.hasFields = !fields.isEmpty();
        this.aggregation.setStep(step);
    }

    @Override
    public void collect(int doc) throws IOException {
        matchingDocCount.incrementAndGet();
        StoredFields storedFields = context.reader().storedFields();
        Document document = hasFields ? storedFields.document(doc, allFields) : storedFields.document(doc);
        long timestamp = getTimestamp(document);
        if (timestamp == 0) return;
        for (IndexableField field : document.getFields()) {
            if (hasFields && fields.contains(field.name())) {
                processDocument(document, field, timestamp);
            } else if (!(hasFields || SearchUtils.isStandardFieldName(field.name()))) {
                processDocument(document, field, timestamp);
            }
        }
    }

    private void processDocument(Document document, IndexableField field, long timestamp) {
        String value = document.get(field.name());
        if (value != null) {
            aggregation.add(Metric.create(field.name(), field.name(), value), Value.create(timestamp, 1));
        }
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
        private final Duration step;
        private final Set<String> fields;

        private Collection<Matrix> trends = Collections.emptyList();
        private int docCount;
        private int matchingDocCount;

        public Manager(String timestampField, Set<String> fields, Duration step) {
            requireNonNull(timestampField);
            requireNonNull(fields);
            requireNonNull(step);
            this.timestampField = timestampField;
            this.fields = new HashSet<>(fields);
            this.step = step;
        }

        int getDocCount() {
            return docCount;
        }

        int getMatchingDocCount() {
            return matchingDocCount;
        }

        Collection<Matrix> getTrends() {
            return trends;
        }

        @Override
        public FieldTrendCollector newCollector() throws IOException {
            return new FieldTrendCollector(timestampField, fields, step);
        }

        @Override
        public Integer reduce(Collection<FieldTrendCollector> collectors) throws IOException {
            Aggregation aggregation = new Aggregation().setStep(step);
            for (FieldTrendCollector collector : collectors) {
                matchingDocCount += collector.matchingDocCount.get();
                docCount += collector.docCount.get();
                aggregation.merge(collector.aggregation);
            }
            this.trends = aggregation.toMatrixes();
            return this.trends.size();
        }
    }
}
