package net.microfalx.bootstrap.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Transforms the query to a wildcard query if the query is not already a specialized query.
     *
     * @param query                the query
     * @param autoWildcard         true to create wildcard searches where possible
     * @param allowLeadingWildcard <code>true</code>
     * @return a wildcard query or the original query
     */
    public static String normalizeQuery(String query, boolean autoWildcard, boolean allowLeadingWildcard) {
        requireNonNull(query);

        if (query.contains("\"") || query.contains("(")) {
            // do not handle phrases or groups
            return query;
        }

        String[] parts = StringUtils.split(query, " ");
        StringBuilder buffer = new StringBuilder();
        for (String part : parts) {
            String term = part;
            int fieldNamePos = term.indexOf(':');
            boolean leadingWildcardAdded = false;
            if (fieldNamePos != -1) {
                String fieldName = term.substring(0, fieldNamePos);
                if (FIELD_NAMES.contains(fieldName)) {
                    // it is a field
                    buffer.append(fieldName).append(":");
                } else {
                    if (autoWildcard && allowLeadingWildcard) {
                        leadingWildcardAdded = true;
                        buffer.append("*");
                    }
                    buffer.append(fieldName).append("\\:");
                }
                term = term.substring(fieldNamePos + 1);
            }
            if (hasTermSpecialChars(term) || isOperator(term)) {
                buffer.append(term);
            } else {
                if (autoWildcard && allowLeadingWildcard && !leadingWildcardAdded) {
                    buffer.append("*");
                }
                buffer.append(term);
                if (autoWildcard) {
                    buffer.append("*");
                }
                buffer.append(' ');
            }
        }
        return buffer.toString().trim();
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
                while (priorityQueue.size() > maxTerms) {
                    priorityQueue.poll();
                }
                fieldStatistic.setTerms(priorityQueue.stream().sorted(comparingLong(TermStatistics::getCount).reversed()).toList());
            } catch (Exception e) {
                LOGGER.warn("Failed to extract terms for '" + fieldName + ", root cause: " + e.getMessage());
            }
        }
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
