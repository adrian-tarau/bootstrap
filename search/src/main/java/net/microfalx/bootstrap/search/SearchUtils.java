package net.microfalx.bootstrap.search;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.bootstrap.search.Document.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Various utilities for search engine
 */
public class SearchUtils {

    public static final String DEFAULT_FIELD = DESCRIPTION_FIELD;

    public static final long NA_TIMESTAMP = 0;

    /**
     * A set containing all the standard field names
     */
    private static final Set<String> STANDARD_FIELD_NAMES = new HashSet<>();

    /**
     * A set containing all numeric field names
     */
    private static final Set<String> NUMERIC_FIELD_NAMES = new HashSet<>();

    /**
     * A set which contains all available field names extracted from index.
     * This information is used to differentiate IPv6 terms and insert wildcards when not provided
     */
    static final Set<String> FIELD_NAMES = new HashSet<>();

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
     * <p/>
     * Current normalizations:
     * <ul>
     * <li>IPv4/IPv6 normalization (eliminated unwanted zeros</li>
     * </ul>
     *
     * @param text the text to normalize
     * @return the normalized text
     */
    public static String normalizeText(String text) {
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

        NUMERIC_FIELD_NAMES.add(CREATED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(MODIFIED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(RECEIVED_AT_FIELD);
        NUMERIC_FIELD_NAMES.add(SENT_AT_FIELD);

        FIELD_NAMES.addAll(STANDARD_FIELD_NAMES);

    }

}