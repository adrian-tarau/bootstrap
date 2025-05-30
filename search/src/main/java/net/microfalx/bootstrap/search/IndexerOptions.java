package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.analysis.Analyzer;

/**
 * Represents options for configuring the indexer.
 */
@Getter
@Setter
public class IndexerOptions {

    /**
     * An analyzer used for indexing.
     */
    private Analyzer analyzer = Analyzers.createIndexAnalyzer();

    /**
     * Indicates whether the index should be recreated.
     */
    private boolean recreate;

    public static IndexerOptions create() {
        return new IndexerOptions();
    }

    private IndexerOptions() {
    }
}
