package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.Analyzer;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

/**
 * Options for configuring the searcher.
 * <p>
 * This class allows you to specify the thread pool to use for executing search requests
 * and the interval at which the search index is refreshed.
 */
@Getter
@Setter
public class SearcherOptions {

    /**
     * An analyzer used for searching.
     */
    private Analyzer analyzer = Analyzers.createSearchAnalyzer();

    /**
     * The thread pool to use for executing search requests.
     */
    private ThreadPool threadPool;

    /**
     * The interval at which the search index is refreshed.
     * <p>
     * This is used to control how often the search index is updated with new data.
     * The default value is 60 seconds.
     */
    private Duration refreshInterval = ofSeconds(60);

    public static SearcherOptions create() {
        return new SearcherOptions();
    }

    private SearcherOptions() {
    }
}
