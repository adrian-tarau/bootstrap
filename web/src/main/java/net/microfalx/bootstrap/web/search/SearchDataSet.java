package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.SearchQuery;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.search.SearchUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Provider
public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

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
        result.setTimestamp(document.getTimestamp().toLocalDateTime());
        result.setName(document.getName());
        result.setType(document.getType());
        result.setOwner(document.getOwner());
        result.setDescription(document.getDescription());
        result.setAttributes(SearchUtils.sort(document.getAttributes()));
        return result;
    }

    private String getQueryString(Filter filter) {
        return StringUtils.EMPTY_STRING;
    }
}
