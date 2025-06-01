package net.microfalx.bootstrap.search;

import lombok.Getter;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.metrics.Metrics;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.Analyzer;

import java.io.File;

/**
 * Base class for searcher and indexer options.
 */
@Getter
abstract class BaseOptions extends NamedAndTaggedIdentifyAware<String> {

    /**
     * The directory where the index will be stored.
     * <p>
     * This directory must be writable and accessible by the application.
     */
    File directory;

    /**
     * An analyzer used for indexing or searching.
     */
    private Analyzer analyzer;

    /**
     * The metrics instance used for tracking indexing or searching performance.
     */
    private Metrics metrics;

    /**
     * The thread pool to use for indexing or searching operations.
     */
    ThreadPool threadPool;

    public static abstract class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private File directory;
        private Analyzer analyzer = Analyzers.createIndexAnalyzer();
        private Metrics metrics = SearchUtils.INDEX_METRICS;
        private ThreadPool threadPool;
        private boolean recreate;
        private boolean main;

        public Builder(String id) {
            super(id);
        }

        public final Builder directory(File directory) {
            this.directory = directory;
            return this;
        }

        public final Builder analyzer(Analyzer analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public final Builder metrics(Metrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public final Builder threadPool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return this;
        }

        public BaseOptions build() {
            BaseOptions options = (BaseOptions) super.build();
            options.directory = directory;
            options.analyzer = analyzer;
            options.metrics = metrics;
            options.threadPool = threadPool;
            return options;
        }
    }
}
