package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.ComparisonExpression;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.SearchQuery;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.microfalx.bootstrap.model.AttributeConstants.DEFAULT_MAXIMUM_ATTRIBUTES;
import static net.microfalx.bootstrap.model.FieldConstants.CREATED_AT;
import static net.microfalx.bootstrap.model.FieldConstants.MODIFIED_AT;
import static net.microfalx.lang.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Provider
public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

    private static final int MAX_DESCRIPTION_LENGTH = 150;

    private SearchService searchService;

    public SearchDataSet(DataSetFactory<SearchResult, PojoField<SearchResult>, String> factory, Metadata<SearchResult, PojoField<SearchResult>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        searchService = getService(SearchService.class);
    }

    @Override
    protected Optional<SearchResult> doFindById(String id) {
        SearchResult searchResult = convert(searchService.find(id));
        return Optional.ofNullable(searchResult);
    }

    @Override
    protected Page<SearchResult> doFindAll(Pageable pageable, Filter filterable) {
        net.microfalx.bootstrap.search.SearchResult result = searchService.search(convert(pageable, filterable));
        return convert(pageable, result);
    }

    private SearchQuery convert(Pageable pageable, Filter filterable) {
        SearchQuery query = new SearchQuery(getQuery(filterable));
        query.setStart((int) pageable.getOffset()).setLimit((int) (pageable.getOffset() + pageable.getPageSize()));
        updateTimeFilter(filterable, query);
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.stream().iterator().next();
            if (CREATED_AT.equalsIgnoreCase(order.getProperty()) || MODIFIED_AT.equalsIgnoreCase(order.getProperty())) {
                String field = CREATED_AT.equalsIgnoreCase(order.getProperty()) ? Document.CREATED_AT_FIELD : Document.MODIFIED_AT_FIELD;
                query.setSort(new SearchQuery.Sort(SearchQuery.Sort.Type.FIELD, field, order.isDescending()));
            }
        }
        return query;
    }

    private Page<SearchResult> convert(Pageable pageable, net.microfalx.bootstrap.search.SearchResult result) {
        List<SearchResult> results = new ArrayList<>();
        for (Document document : result.getDocuments()) {
            results.add(convert(document));
        }
        PageImpl<SearchResult> dataSetPage = new PageImpl<>(results, pageable, result.getTotalHits());
        return dataSetPage;
    }

    private SearchResult convert(Document document) {
        if (document == null) return null;
        SearchResult result = new SearchResult();
        result.setId(document.getId());
        result.setCreatedAt(document.getCreatedAt().toLocalDateTime());
        result.setModifiedAt(document.getModifiedAt().toLocalDateTime());
        result.setName(document.getName());
        result.setDescription(document.getDescription());
        result.setTitle(abbreviate(defaultIfEmpty(document.getDescription(), document.getName()), MAX_DESCRIPTION_LENGTH));
        result.setType(document.getType());
        result.setMimeType(document.getMimeType());
        result.setOwner(document.getOwner());
        result.setRelevance(document.getRelevance());
        result.setLength(document.getLength());
        result.setAttributeCount(document.toMap().size());
        result.setTopAttributes(document.toCollection(DEFAULT_MAXIMUM_ATTRIBUTES, attribute -> searchService.accept(document, attribute)));
        result.setAttributes(document.toCollection());
        result.setBody(document.getBody());
        return result;
    }

    private String getQuery(Filter filter) {
        ComparisonExpression expression = filter.findExpression(ComparisonExpression.QUERY);
        return expression != null ? defaultIfNull((String) expression.getValue(), EMPTY_STRING) : EMPTY_STRING;
    }

    private void updateTimeFilter(Filter filter, SearchQuery query) {
        ComparisonExpression expression = filter.findExpression("modifiedAt");
        if (expression == null) return;
        query.setStartTime((ZonedDateTime) expression.getValues()[0]).setEndTime((ZonedDateTime) expression.getValues()[1]);
    }
}
