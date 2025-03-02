package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.metrics.AbstractRepository;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.metrics.Matrix;
import net.microfalx.metrics.Query;
import net.microfalx.metrics.Result;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Provider
public class SearchRepository extends AbstractRepository {

    private static final String QUERY_TYPE_PREFIX = "search.";
    static final String DOCUMENT_QUERY_TYPE = QUERY_TYPE_PREFIX + "document";
    static final String FIELD_QUERY_TYPE = QUERY_TYPE_PREFIX + "field";

    @Override
    public boolean supports(Query query) {
        return DOCUMENT_QUERY_TYPE.equalsIgnoreCase(query.getType()) || FIELD_QUERY_TYPE.equalsIgnoreCase(query.getType());
    }

    @Override
    public Result query(Query query) {
        requireNonNull(query);
        SearchQuery newQuery = convertQuery(query);
        if (DOCUMENT_QUERY_TYPE.equalsIgnoreCase(query.getType())) {
            Matrix matrix = getSearchService().getDocumentTrends(newQuery, Document.CREATED_AT_FIELD, query.getStep());
            return Result.matrix(query, singletonList(matrix));
        } else {
            Collection<Matrix> matrixes = getSearchService().getFieldsTrends(newQuery, Document.CREATED_AT_FIELD, query.getStep());
            return Result.matrix(query, matrixes);
        }
    }

    private SearchService getSearchService() {
        return getBean(SearchService.class);
    }

    private SearchQuery convertQuery(Query query) {
        SearchQuery newQuery = new SearchQuery(query.getText()).setStartTime(query.getStartTime()).setEndTime(query.getEndTime());
        return newQuery;
    }
}
