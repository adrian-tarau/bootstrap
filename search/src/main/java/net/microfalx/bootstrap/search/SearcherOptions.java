package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;

import java.time.Duration;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Options for configuring the searcher.
 * <p>
 * This class allows you to specify the thread pool to use for executing search requests
 * and the interval at which the search index is refreshed.
 */
@Getter
@ToString
public class SearcherOptions extends BaseOptions {

    /**
     * The interval at which the search index is refreshed.
     * <p>
     * This is used to control how often the search index is updated with new data.
     * The default value is 60 seconds.
     */
    private Duration refreshInterval;

    /**
     * Creates a new builder for SearcherOptions.
     *
     * @param id the identifier for the index
     * @return a new Builder instance
     */
    public static Builder create(String id) {
        return new Builder(id);
    }

    public static final class Builder extends BaseOptions.Builder {

        private Duration refreshInterval = ofSeconds(60);

        public Builder(String id) {
            super(id);
            metrics(SearchUtils.SEARCH_METRICS);
        }

        public Builder refreshInterval(Duration refreshInterval) {
            requireNonNull(refreshInterval);
            this.refreshInterval = refreshInterval;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new SearcherOptions();
        }

        public SearcherOptions build() {
            SearcherOptions options = (SearcherOptions) super.build();
            options.refreshInterval = refreshInterval;
            return options;
        }
    }
}
