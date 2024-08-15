package net.microfalx.bootstrap.search;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Holds the result of a search.
 */
public class SearchResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 8667692188788793737L;

    private final SearchQuery query;
    private String rewriteQuery;
    private String suggestedQuery;
    private List<Document> documents = Collections.emptyList();
    private Map<String, Document> itemsById;
    private long totalHits;

    public SearchResult(SearchQuery query) {
        requireNonNull(query);
        this.query = query;
    }

    public String getId() {
        return query.getId();
    }

    public SearchQuery getQuery() {
        return query;
    }

    public String getOriginalQuery() {
        return query.getQuery();
    }

    public String getRewriteQuery() {
        return rewriteQuery;
    }

    protected void setRewriteQuery(String rewriteQuery) {
        this.rewriteQuery = rewriteQuery;
    }

    public String getSuggestedQuery() {
        return suggestedQuery;
    }

    protected void setSuggestedQuery(String suggestedQuery) {
        this.suggestedQuery = suggestedQuery;
    }

    public Document getDocument(String id) {
        requireNonNull(id);
        if (itemsById == null) {
            itemsById = new HashMap<>();
            documents.forEach(document -> itemsById.put(document.getId(), document));
        }
        return itemsById.get(id);
    }

    public List<Document> getDocuments() {
        return unmodifiableList(documents);
    }

    protected void setDocuments(List<Document> documents) {
        this.documents = defaultIfNull(documents, Collections.emptyList());
    }

    public long getTotalHits() {
        return totalHits;
    }

    protected void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchResult{");
        sb.append("query='").append(query).append('\'');
        sb.append(", rewriteQuery='").append(rewriteQuery).append('\'');
        sb.append(", suggestedQuery='").append(suggestedQuery).append('\'');
        sb.append(", totalHits=").append(totalHits);
        sb.append(", items=").append(documents.size());
        sb.append('}');
        return sb.toString();
    }
}
