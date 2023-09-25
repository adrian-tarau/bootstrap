package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.search.*;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;

@Provider
public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

    private static final String CREATED_AT = "createdAt";
    private static final String MODIFIED_AT = "modifiedAt";
    private static final int MAX_DESCRIPTION_LENGTH = 150;

    public SearchDataSet(DataSetFactory<SearchResult, PojoField<SearchResult>, String> factory, Metadata<SearchResult, PojoField<SearchResult>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<SearchResult> doFindAll(Pageable pageable, Filter filterable) {
        SearchService searchService = getService(SearchService.class);
        net.microfalx.bootstrap.search.SearchResult result = searchService.search(convert(pageable, filterable));
        return convert(pageable, result);
    }

    private SearchQuery convert(Pageable pageable, Filter filterable) {
        SearchQuery query = new SearchQuery(getQueryString(filterable));
        query.setStart((int) pageable.getOffset()).setLimit((int) (pageable.getOffset() + pageable.getPageSize()));
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
        SearchResult result = new SearchResult();
        result.setId(document.getId());
        result.setCreatedAt(document.getCreatedAt().toLocalDateTime());
        result.setModifiedAt(document.getCreatedAt().toLocalDateTime());
        result.setDescription(org.apache.commons.lang3.StringUtils.abbreviate(defaultIfEmpty(document.getDescription(), document.getName()), MAX_DESCRIPTION_LENGTH));
        result.setType(document.getType());
        result.setOwner(document.getOwner());
        result.setRelevance(document.getRelevance());
        result.setAttributes(SearchUtils.sort(filter(document, document.getAttributes())));
        return result;
    }

    private Collection<Attribute> filter(Document document, Collection<Attribute> attributes) {
        SearchService searchService = getService(SearchService.class);
        return attributes.stream().filter(attribute -> searchService.accept(document, attribute)).toList();
    }

    private String getQueryString(Filter filter) {
        return StringUtils.EMPTY_STRING;
    }
}
