package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.metrics.Metrics;
import org.apache.lucene.analysis.Analyzer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

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
     * The metrics instance used for tracking indexing performance.
     */
    private Metrics metrics = SearchUtils.INDEX_METRICS;

    /**
     * Indicates whether the index should be recreated.
     */
    private boolean recreate;

    public static IndexerOptions create() {
        return new IndexerOptions();
    }

    private IndexerOptions() {
    }

    /**
     * Changes the analyzer used for indexing.
     *
     * @param analyzer the new analyzer to use
     * @return self
     */
    public IndexerOptions setAnalyzer(Analyzer analyzer) {
        requireNonNull(metrics);
        this.analyzer = analyzer;
        return this;
    }

    /**
     * Changes the metrics instance used for tracking indexing performance.
     *
     * @param metrics the new metrics instance to use
     * @return self
     */
    public IndexerOptions setMetrics(Metrics metrics) {
        requireNonNull(metrics);
        this.metrics = metrics;
        return this;
    }
}
