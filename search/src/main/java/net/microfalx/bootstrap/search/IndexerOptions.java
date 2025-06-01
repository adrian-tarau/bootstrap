package net.microfalx.bootstrap.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.metrics.Metrics;
import org.apache.lucene.analysis.Analyzer;

import java.io.File;
import java.util.Objects;

/**
 * Represents options for configuring the indexer.
 */
@Getter
@ToString
@Builder
public class IndexerOptions implements Identifiable<String>, Nameable, Descriptable {

    /**
     * The unique identifier for the indexer.
     * <p>
     * This identifier is used to distinguish different indexers.
     */
    private final String id;

    /**
     * The name of the indexer.
     * <p>
     * This name is used for display purposes and should be unique.
     */
    private String name;

    /**
     * A description of the indexer.
     * <p>
     * This description provides additional information about the indexer.
     */
    private String description;

    /**
     * The directory where the index will be stored.
     * <p>
     * This directory must be writable and accessible by the application.
     */
    private File directory;

    /**
     * An analyzer used for indexing.
     */
    @Builder.Default private Analyzer analyzer = Analyzers.createIndexAnalyzer();

    /**
     * The metrics instance used for tracking indexing performance.
     */
    @Builder.Default private Metrics metrics = SearchUtils.INDEX_METRICS;

    /**
     * Indicates whether the index should be recreated.
     */
    private boolean recreate;

    /**
     * Indicates whether this indexer is the main indexer.
     */
    private boolean main;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof IndexerOptions options)) return false;
        return Objects.equals(id, options.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
