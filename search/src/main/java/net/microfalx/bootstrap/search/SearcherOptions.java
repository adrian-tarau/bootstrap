package net.microfalx.bootstrap.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.metrics.Metrics;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.Analyzer;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

import static java.time.Duration.ofSeconds;

/**
 * Options for configuring the searcher.
 * <p>
 * This class allows you to specify the thread pool to use for executing search requests
 * and the interval at which the search index is refreshed.
 */
@Getter
@ToString
@Builder
public class SearcherOptions {

    /**
     * The unique identifier for the searcher.
     * <p>
     * This identifier is used to distinguish different searchers.
     */
    private final String id;

    /**
     * The name of the searcher.
     * <p>
     * This name is used for display purposes and should be unique.
     */
    private String name;

    /**
     * A description of the searcher.
     * <p>
     * This description provides additional information about the searcher.
     */
    private String description;

    /**
     * The directory where the index is stored.
     * <p>
     * This directory must be accessible by the application.
     */
    private File directory;

    /**
     * An analyzer used for searching.
     */
    @Builder.Default private Analyzer analyzer = Analyzers.createSearchAnalyzer();

    /**
     * The thread pool to use for executing search requests.
     */
    private ThreadPool threadPool;

    /**
     * The metrics instance used for tracking search performance.
     */
    @Builder.Default private Metrics metrics = SearchUtils.SEARCH_METRICS;

    /**
     * The interval at which the search index is refreshed.
     * <p>
     * This is used to control how often the search index is updated with new data.
     * The default value is 60 seconds.
     */
    @Builder.Default private Duration refreshInterval = ofSeconds(60);

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof SearcherOptions options)) return false;
        return Objects.equals(id, options.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
