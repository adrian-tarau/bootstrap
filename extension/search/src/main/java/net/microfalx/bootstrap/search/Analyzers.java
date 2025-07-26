package net.microfalx.bootstrap.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.classic.ClassicTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory;
import org.apache.lucene.queryparser.classic.QueryParser;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which creates Lucene analyzers.
 */
public class Analyzers {

    /**
     * Creates an analyzer used during searching.
     * <p>
     * Unless configuration is changed, the same analyzer used for indexing is used for searching too.
     *
     * @return a non-null instance
     */
    public static Analyzer createSearchAnalyzer() {
        return createIndexAnalyzer();
    }

    /**
     * Creates an analyzer used during indexing & searching.
     *
     * @return a non-null instance
     */
    public static Analyzer createIndexAnalyzer() {
        try {
            CustomAnalyzer.Builder builder = CustomAnalyzer.builder(new SearchResourceLoader())
                    .withTokenizer(ClassicTokenizerFactory.NAME)
                    .addTokenFilter(EnglishPossessiveFilterFactory.NAME)
                    .addTokenFilter(LowerCaseFilterFactory.NAME)
                    .addTokenFilter(StopFilterFactory.NAME, "ignoreCase", "true",
                            "words", "stopwords.txt", "format", "wordset");
            return builder.build();
        } catch (Exception e) {
            throw new IndexException("Could not create analyzer", e);
        }
    }

    /**
     * Creates a query parser
     *
     * @param defaultField the default field name
     * @return a non-null instance
     */
    public static QueryParser createQueryParser(String defaultField) {
        requireNonNull(defaultField);
        QueryParser queryParser = new QueryParser(defaultField, createIndexAnalyzer());
        return queryParser;
    }

    /**
     * Creates query parser using the default field name.
     *
     * @return a non-null instance
     */
    public static QueryParser createQueryParser() {
        return createQueryParser(Document.BODY_FIELD);
    }
}
