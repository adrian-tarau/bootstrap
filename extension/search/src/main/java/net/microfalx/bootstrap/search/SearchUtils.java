package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.ResourceUtils;
import net.microfalx.threadpool.ThreadPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Comparator.comparingLong;
import static net.microfalx.bootstrap.search.Document.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Various utilities for search engine
 */
public class SearchUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchUtils.class);

    public static Metrics INDEX_METRICS = Metrics.of("Index");
    public static Metrics SEARCH_METRICS = Metrics.of("Search");

    public static final String DEFAULT_FIELD = BODY_FIELD;

    /**
     * A long value which represents "timestamp is not available"
     */
    public static final long NA_TIMESTAMP = 0;

    /**
     * The maximum size for a field in lucene
     */
    public static final int MAX_FIELD_LENGTH = 32000;

    /**
     * The maximum number of term statistics tracked by field
     */
    public static final int MAX_TERMS_PER_FIELD = 20;

    /**
     * The maximum number of terms scanned from the index to extract statistics
     */
    public static final int MAX_TERMS_SCANNED = MAX_TERMS_PER_FIELD * 50;

    /**
     * A set containing all the standard field names.
     */
    private static final Set<String> STANDARD_FIELD_NAMES = new HashSet<>();

    /**
     * A set containing all the metadata field names.
     */
    private static final Set<String> METADATA_FIELD_NAMES = new HashSet<>();

    /**
     * A set containing all numeric field names.
     */
    private static final Set<String> NUMERIC_FIELD_NAMES = new HashSet<>();

    /**
     * A set which contains all available field names extracted from index.
     * This information is used to differentiate IPv6 terms and insert wildcards when not provided
     */
    static final Set<String> FIELD_NAMES = new HashSet<>();

    /**
     * The operator injected when the user clicks on a field value in the grid.
     */
    public final static String DEFAULT_FILTER_OPERATOR = ": ";

    /**
     * Returns the operator injected when the user clicks on a field value in the grid.
     */
    public final static char DEFAULT_FILTER_QUOTE_CHAR = '"';

    final static String INDEX_NAME = "primary";

    /**
     * Returns whether the given field name is part of the standard field names.
     *
     * @param name the field name
     * @return <code>true</code> if a standard field name, <code>false</code> otherwise
     */
    public static boolean isStandardFieldName(String name) {
        return STANDARD_FIELD_NAMES.contains(name);
    }

    /**
     * Returns whether the given field name is part of the metadata field names.
     *
     * @param name the field name
     * @return <code>true</code> if a metadata field name, <code>false</code> otherwise
     */
    public static boolean isMetadataFieldName(String name) {
        return METADATA_FIELD_NAMES.contains(name);
    }

    /**
     * Transforms the query and escapes if requested.
     *
     * @param query                the query
     * @param escape   {@code true} if the query should be escaped, {@code false} otherwise
     * @return a wildcard query or the original query
     */
    public static String normalizeQuery(String query, boolean escape) {
        if (StringUtils.isEmpty(query)) return query;
        if (escape) query = net.microfalx.lang.StringUtils.replaceAll(query, "/", "\\/");
        return query;
    }

    /**
     * Normalizes text by eliminating or transforming characters.
     *
     * @param text the text to normalize
     * @return the normalized text
     */
    public static String normalizeText(String text, boolean field) {
        if (text == null) return null;
        if (field) text = StringUtils.abbreviate(text, MAX_FIELD_LENGTH);
        return text;
    }

    /**
     * Returns whether the term is an operator.
     *
     * @param term the term
     * @return <code>true</code> if operator, <code>false</code> otherwise
     */
    public static boolean isOperator(String term) {
        for (String operator : OPERATORS) {
            if (term.equalsIgnoreCase(operator)) return true;
        }
        return false;
    }

    /**
     * Returns whether the term contains special chars.
     *
     * @return <code>true</code> if custom query, <code>false</code> if a simple query
     */
    public static boolean hasTermSpecialChars(String term) {
        for (char searchEngineSpecialChar : TERM_SPECIAL_CHARS) {
            if (term.contains(String.valueOf(searchEngineSpecialChar))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the query contains special chars.
     *
     * @return <code>true</code> if custom query, <code>false</code> if a simple query
     */
    public static boolean hasQuerySpecialChars(String query) {
        for (char searchEngineSpecialChar : QUERY_PARSER_SPECIAL_CHARS) {
            if (query.contains(String.valueOf(searchEngineSpecialChar))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the field represent one of the numerical fields.
     *
     * @param name the field name
     * @return {@code true} if numeric, {@code false} otherwise
     */
    public static boolean isNumericField(String name) {
        requireNonNull(name);
        return NUMERIC_FIELD_NAMES.contains(name.toLowerCase());
    }

    /**
     * Returns the fields from an index.
     *
     * @param reader the reader
     * @return the fields
     */
    public static Fields getFields(IndexReader reader) {
        requireNonNull(reader);
        final List<LeafReaderContext> leaves = reader.leaves();
        final List<Fields> fields = new ArrayList<>(leaves.size());
        final List<ReaderSlice> slices = new ArrayList<>(leaves.size());
        for (final LeafReaderContext ctx : leaves) {
            final LeafReader r = ctx.reader();
            final Fields f = new LeafReaderFields(r);
            fields.add(f);
            slices.add(new ReaderSlice(ctx.docBase, r.maxDoc(), fields.size() - 1));
        }
        if (fields.size() == 1) {
            return fields.get(0);
        } else {
            return new MultiFields(fields.toArray(Fields.EMPTY_ARRAY), slices.toArray(ReaderSlice.EMPTY_ARRAY));
        }
    }

    /**
     * Extracts fields and terms from an index.
     *
     * @param indexReader the reader
     * @param fields      the map to collect fields
     * @param maxTerms    the maximum number of terms per field
     */
    public static void extractFieldsAndTerms(IndexReader indexReader, Map<String, FieldStatistics> fields, int maxTerms) {
        Fields luceneFields = SearchUtils.getFields(indexReader);
        for (String fieldName : luceneFields) {
            if (BODY_FIELD.equals(fieldName)) continue;
            try {
                Terms terms = luceneFields.terms(fieldName);
                if (terms == null) continue;
                FieldStatistics fieldStatistic = new FieldStatistics(fieldName);
                fieldStatistic.documentCount = terms.getDocCount();
                fields.put(toIdentifier(fieldName), fieldStatistic);
                fieldStatistic.termCount = (int) terms.size();
                boolean countTerms = fieldStatistic.termCount <= 0;
                if (countTerms) fieldStatistic.termCount = 0;
                int maxTermsScanned = MAX_TERMS_SCANNED;
                TermsEnum iterator = terms.iterator();
                BytesRef byteRef;
                PriorityQueue<TermStatistics> priorityQueue = new PriorityQueue<>(comparingLong(TermStatistics::getCount));
                while ((byteRef = iterator.next()) != null && maxTermsScanned-- > 0) {
                    String term = byteRef.utf8ToString();
                    Term termInstance = new Term(fieldName, byteRef);
                    TermStatistics termStatistics = new TermStatistics(fieldName, term);
                    termStatistics.frequency = indexReader.totalTermFreq(termInstance);
                    termStatistics.count = indexReader.docFreq(termInstance);
                    fieldStatistic.termCount += termStatistics.count;
                    priorityQueue.add(termStatistics);
                    if (priorityQueue.size() >= 5 * maxTerms) {
                        priorityQueue.poll();
                    }
                }
                boolean incomplete = priorityQueue.size() > maxTerms;
                while (priorityQueue.size() > maxTerms) {
                    priorityQueue.poll();
                }
                fieldStatistic.setTerms(priorityQueue.stream().sorted(comparingLong(TermStatistics::getCount).reversed()).toList(), incomplete);
            } catch (Exception e) {
                LOGGER.warn("Failed to extract terms for '" + fieldName + ", root cause: " + e.getMessage());
            }
        }
    }

    /**
     * Returns whether the index is unusable due to a Lucene exception.
     *
     * @param throwable the throwable
     * @return <code>true</code> if the index is unusable, <code>false</code> otherwise
     */
    public static boolean isIndexUnusable(Throwable throwable) {
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        if (rootCause == null) rootCause = throwable;
        return rootCause instanceof AlreadyClosedException || rootCause instanceof CorruptIndexException;
    }

    /**
     * Update the options based on service configuration
     */
    static void updateOptions(ResourceService resourceService, ThreadPool threadPool, BaseOptions options) {
        threadPool = ObjectUtils.defaultIfNull(threadPool, ThreadPool.get());
        if (options.directory == null) {
            File directory = ResourceUtils.toFile(resourceService.getPersisted("search"));
            options.directory = FileUtils.validateDirectoryExists(new File(directory, options.getId()));
        }
        if (options.threadPool == null) options.threadPool = threadPool;
    }

    /**
     * Creates the retry template to include a listener for Lucene exceptions.
     *
     * @return the updated retry template
     */
    static RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        template.registerListener(new LuceneRetryListener());
        return template;
    }

    private final static String[] OPERATORS = new String[]{
            "and", "or", "not", "+", "!"
    };

    private final static char[] TERM_SPECIAL_CHARS = new char[]{
            '?', '*', '\\', '~', '^', '+', '!'
    };

    private final static char[] QUERY_PARSER_SPECIAL_CHARS = new char[]{
            ':', '"', '\\', '+'
    };

    static {
        STANDARD_FIELD_NAMES.add(ID_FIELD);
        STANDARD_FIELD_NAMES.add(NAME_FIELD);
        STANDARD_FIELD_NAMES.add(DESCRIPTION_FIELD);
        STANDARD_FIELD_NAMES.add(BODY_FIELD);
        STANDARD_FIELD_NAMES.add(BODY_URI_FIELD);
        STANDARD_FIELD_NAMES.add(TYPE_FIELD);
        STANDARD_FIELD_NAMES.add(OWNER_FIELD);
        STANDARD_FIELD_NAMES.add(LENGTH_FIELD);
        STANDARD_FIELD_NAMES.add(MIME_TYPE_FIELD);
        STANDARD_FIELD_NAMES.add(REFERENCE_FIELD);
        STANDARD_FIELD_NAMES.add(TAG_FIELD);
        STANDARD_FIELD_NAMES.add(USER_DATA_FIELD);
        STANDARD_FIELD_NAMES.add(CREATED_AT_FIELD);
        STANDARD_FIELD_NAMES.add(CREATED_AT_FIELD + STORED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(CREATED_AT_FIELD + SORTED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(MODIFIED_AT_FIELD);
        STANDARD_FIELD_NAMES.add(MODIFIED_AT_FIELD + STORED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(MODIFIED_AT_FIELD + SORTED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(RECEIVED_AT_FIELD);
        STANDARD_FIELD_NAMES.add(RECEIVED_AT_FIELD + STORED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(RECEIVED_AT_FIELD + SORTED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(SENT_AT_FIELD);
        STANDARD_FIELD_NAMES.add(SENT_AT_FIELD + STORED_SUFFIX_FIELD);
        STANDARD_FIELD_NAMES.add(SENT_AT_FIELD + SORTED_SUFFIX_FIELD);

        METADATA_FIELD_NAMES.add(BODY_FIELD);
        METADATA_FIELD_NAMES.add(BODY_URI_FIELD);
        METADATA_FIELD_NAMES.add(CREATED_AT_FIELD);
        METADATA_FIELD_NAMES.add(RECEIVED_AT_FIELD);
        METADATA_FIELD_NAMES.add(MODIFIED_AT_FIELD);
        METADATA_FIELD_NAMES.add(SENT_AT_FIELD);

        NUMERIC_FIELD_NAMES.add(CREATED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(MODIFIED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(RECEIVED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(SENT_AT_FIELD);

        FIELD_NAMES.addAll(STANDARD_FIELD_NAMES);

    }

    private static class LuceneRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            if (isIndexUnusable(throwable)) context.setExhaustedOnly();
        }
    }

    private static class LeafReaderFields extends Fields {

        private final LeafReader leafReader;
        private final List<String> indexedFields;

        LeafReaderFields(LeafReader leafReader) {
            this.leafReader = leafReader;
            this.indexedFields = new ArrayList<>();
            for (FieldInfo fieldInfo : leafReader.getFieldInfos()) {
                if (fieldInfo.getIndexOptions() != IndexOptions.NONE) {
                    indexedFields.add(fieldInfo.name);
                }
            }
            Collections.sort(indexedFields);
        }

        @Override
        public Iterator<String> iterator() {
            return Collections.unmodifiableList(indexedFields).iterator();
        }

        @Override
        public int size() {
            return indexedFields.size();
        }

        @Override
        public Terms terms(String field) throws IOException {
            return leafReader.terms(field);
        }
    }

}
