package net.microfalx.bootstrap.search;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;

/**
 * Represents options for configuring the indexer.
 */
@Getter
@ToString
public class IndexerOptions extends BaseOptions {

    /**
     * Indicates whether the index should be recreated.
     */
    private boolean recreate;

    /**
     * Indicates whether this indexer is the primary indexer.
     */
    private boolean primary;

    /**
     * Creates a new builder for IndexerOptions.
     *
     * @param id the identifier for the index
     * @return a new Builder instance
     */
    public static Builder create(String id) {
        return new Builder(id);
    }

    public static final class Builder extends BaseOptions.Builder {

        private boolean recreate;
        private boolean primary;

        public Builder(String id) {
            super(id);
        }

        public Builder recreate(boolean recreate) {
            this.recreate = recreate;
            return this;
        }

        public Builder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new IndexerOptions();
        }

        public IndexerOptions build() {
            IndexerOptions options = (IndexerOptions) super.build();
            options.recreate = recreate;
            options.primary = primary;
            return options;
        }
    }

}
