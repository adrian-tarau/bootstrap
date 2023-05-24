package net.microfalx.bootstrap.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import static net.microfalx.resource.ResourceUtils.requireNonNull;

/**
 * A class which creates Lucene analyzers.
 */
public class Analyzers {

    /**
     * Creates an analyzer used during searching.
     *
     * @return a non-null instance
     */
    public static Analyzer createSearchAnalyzer() {
        return new WhitespaceAndSpecialCharsAnalyzer();
    }

    /**
     * Creates an analyzer used during indexing.
     *
     * @return a non-null instance
     */
    public static Analyzer createIndexAnalyzer() {
        return new WhitespaceAndSpecialCharsAnalyzer();
    }

    /**
     * Creates a query parser
     *
     * @param defaultField the default field name
     * @return a non-null instance
     */
    public static QueryParser createQueryParser(String defaultField) {
        requireNonNull(defaultField);

        QueryParserImpl queryParser = new QueryParserImpl(defaultField, createIndexAnalyzer());
        return queryParser;
    }

    /**
     * Creates query parser using the default field name.
     *
     * @return a non-null instance
     */
    public static QueryParser createQueryParser() {
        return createQueryParser(SearchUtilities.NAME_FIELD);
    }

    static final class WhitespaceAndSpecialCharsAnalyzer extends Analyzer {

        public WhitespaceAndSpecialCharsAnalyzer() {
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            return new TokenStreamComponents(new WhitespaceTokenizer());
        }
    }

    static final class QueryParserImpl extends QueryParser {

        public QueryParserImpl(String f, Analyzer a) {
            super(f, a);
        }

        @Override
        protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
            return super.getFieldQuery(field, queryText, quoted);
        }
    }

    static final class WhitespaceAndSpecialCharsTokenizer extends CharTokenizer {

        /**
         * Collects only characters which are not white spaces or part of a list of special characters
         *
         * @see Character#isWhitespace
         * @see #SPECIAL_TOKEN_CHARS
         */
        @Override
        protected boolean isTokenChar(int c) {
            return !(isSpecialChar((char) c) || Character.isWhitespace(c));
        }

    }

    /**
     * Returns whether the character is part of a list of special chars.
     *
     * @param c the character to validate
     * @return <code>true</code> if special, <code>false</code> otherwise
     */
    private static boolean isSpecialChar(char c) {
        for (char specialTokenChar : SPECIAL_TOKEN_CHARS) {
            if (specialTokenChar == c) {
                return true;
            }
        }
        return false;
    }

    static final char[] SPECIAL_TOKEN_CHARS = new char[0];

    /*static final char[] SPECIAL_TOKEN_CHARS = new char[]{
            '(', ')',
            '[', ']',
            '/', '\\',
            ',', '_', '-', '+', '=',
            '!', '@', '#', '$', '%', '&', '*',
            '"', '\'', '?'
    };*/
}
