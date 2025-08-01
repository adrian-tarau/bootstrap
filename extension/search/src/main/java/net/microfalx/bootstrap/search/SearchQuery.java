package net.microfalx.bootstrap.search;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Holds search parameters.
 */
public class SearchQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = -2913847551433424494L;

    private final String id = UUID.randomUUID().toString();
    private final String query;
    private String filter;
    private int start = 0;
    private int limit = 500;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private boolean timeless;

    private boolean autoWildcard;
    private boolean allowLeadingWildcard;

    private Sort sort = new Sort(Sort.Type.RELEVANCE);

    public SearchQuery(String query) {
        requireNonNull(query);
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public int getStart() {
        return start;
    }

    public SearchQuery setStart(int start) {
        this.start = start;
        return this;
    }

    public int getLimit() {
        return limit;
    }

    public SearchQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public Sort getSort() {
        return sort;
    }

    public SearchQuery setSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public SearchQuery setFilter(String filter) {
        this.filter = filter;

        return this;
    }

    public ZonedDateTime getStartTime() {
        if (timeless) {
            return null;
        } else {
            return startTime != null ? startTime : ZonedDateTime.now().minusDays(7);
        }
    }

    public SearchQuery setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public ZonedDateTime getEndTime() {
        if (timeless) {
            return null;
        } else {
            return endTime != null ? endTime : ZonedDateTime.now();
        }
    }

    public SearchQuery setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public SearchQuery setTimeless(boolean timeless) {
        this.timeless = timeless;
        return this;
    }

    public boolean isAutoWildcard() {
        return autoWildcard;
    }

    public SearchQuery setAutoWildcard(boolean autoWildcard) {
        this.autoWildcard = autoWildcard;
        return this;
    }

    public boolean isAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }

    public SearchQuery setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.allowLeadingWildcard = allowLeadingWildcard;
        return this;
    }

    public String getDescription() {
        return "query: " + query +
                ", filter: " + filter +
                ", start: " + start +
                ", limit: " + limit +
                ", sort: " + sort.field;
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "id='" + id + '\'' +
                ", query='" + query + '\'' +
                ", filter='" + filter + '\'' +
                ", start=" + start +
                ", limit=" + limit +
                ", autoWildcard=" + autoWildcard +
                ", allowLeadingWildcard=" + allowLeadingWildcard +
                ", sort=" + sort +
                '}';
    }

    /**
     * Holds information about sorting
     */
    public static class Sort implements Serializable {

        @Serial private static final long serialVersionUID = 3914382555374244394L;

        private final Type type;
        private String field;
        private boolean reversed;

        public Sort(Type type) {
            requireNonNull(type);
            this.type = type;
        }

        public Sort(Type type, String field, boolean reversed) {
            requireNonNull(type);
            requireNotEmpty(field);
            this.type = type;
            this.field = field;
            this.reversed = reversed;
        }

        public Type getType() {
            return type;
        }

        public String getField() {
            return field;
        }

        public boolean isReversed() {
            return reversed;
        }

        public enum Type {
            RELEVANCE,
            INDEX_ORDER,
            FIELD
        }

        @Override
        public String toString() {
            return "Sort{" +
                    "type=" + type +
                    ", field='" + field + '\'' +
                    '}';
        }
    }
}
