package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.DataSetUtils;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.ComparisonExpression;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.search.*;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.metrics.Matrix;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static net.microfalx.bootstrap.model.AttributeConstants.DEFAULT_MAXIMUM_ATTRIBUTES;
import static net.microfalx.bootstrap.model.FieldConstants.CREATED_AT;
import static net.microfalx.bootstrap.model.FieldConstants.MODIFIED_AT;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Provider
public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

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
        net.microfalx.bootstrap.search.SearchResult result = searchService.search(convert(filterable, pageable));
        return convert(pageable, result);
    }

    @Override
    public Matrix getTrend(Filter filterable, int points) {
        requireNonNull(filterable);
        SearchQuery query = convert(filterable, null);
        Duration step = DataSetUtils.getStep(query.getStartTime(), query.getEndTime(), points);
        return searchService.getDocumentTrends(query, step);
    }

    @Override
    public Collection<Matrix> getTrend(Filter filterable, Set<String> fields, int points) {
        requireNonNull(filterable);
        requireNonNull(fields);
        if (fields.isEmpty()) fields = getTrendFields();
        SearchQuery query = convert(filterable, null);
        Duration step = DataSetUtils.getStep(query.getStartTime(), query.getEndTime(), points);
        return searchService.getFieldsTrends(query, Document.CREATED_AT_FIELD, fields, step);
    }

    @Override
    public Set<String> getTrendFields() {
        Set<String> fields = new LinkedHashSet<>();
        fields.addAll(Arrays.asList(Document.OWNER_FIELD, Document.TYPE_FIELD, Document.SEVERITY_FIELD, Document.SOURCE_FIELD, Document.TARGET_FIELD));
        Collection<FieldStatistics> fieldStatistics = searchService.getFieldStatistics(20);
        fieldStatistics.forEach(s -> fields.add(s.getName()));
        return fields;
    }

    @Override
    public int getTrendTermCount(String fieldName) {
        FieldStatistics fieldStatistics = searchService.getFieldStatistics(fieldName);
        if (fieldStatistics != null) {
            int termCount = fieldStatistics.getTerms().size();
            if (fieldStatistics.isIncomplete()) termCount = -termCount;
            return termCount;
        } else {
            return 0;
        }
    }

    private SearchQuery convert(Filter filterable, Pageable pageable) {
        SearchQuery query = new SearchQuery(getQuery(filterable));
        updateTimeFilter(filterable, query);
        if (pageable != null) {
            query.setStart((int) pageable.getOffset()).setLimit((int) (pageable.getOffset() + pageable.getPageSize()));
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                Sort.Order order = sort.stream().iterator().next();
                if (CREATED_AT.equalsIgnoreCase(order.getProperty()) || MODIFIED_AT.equalsIgnoreCase(order.getProperty())) {
                    String field = CREATED_AT.equalsIgnoreCase(order.getProperty()) ? Document.CREATED_AT_FIELD : Document.MODIFIED_AT_FIELD;
                    query.setSort(new SearchQuery.Sort(SearchQuery.Sort.Type.FIELD, field, order.isDescending()));
                }
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
        result.setTitle(abbreviate(defaultIfEmpty(document.getName(), document.getDescription()), MAX_DESCRIPTION_LENGTH));
        result.setType(document.getType());
        result.setMimeType(document.getMimeType());
        result.setReference(document.getReference());
        result.setOwner(document.getOwner());
        result.setRelevance(document.getRelevance());
        result.setLength(document.getLength());
        result.setAttributeCount(document.toMap().size());
        result.setTopAttributes(document.toCollection(DEFAULT_MAXIMUM_ATTRIBUTES, attribute -> searchService.accept(document, attribute)));
        result.setAttributes(document.toCollection());
        result.setCoreAttributes(getCoreAttributes(document));
        result.setBody(document.getBody());
        result.setBodyUri(document.getBodyUri());
        return result;
    }

    private List<Attribute> getCoreAttributes(Document document) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(Attribute.create(Document.NAME_FIELD, document.getName()));
        attributes.add(Attribute.create(Document.MIME_TYPE_FIELD, document.getMimeType()));
        attributes.add(Attribute.create(Document.OWNER_FIELD, document.getOwner()));
        attributes.add(Attribute.create(Document.TYPE_FIELD, document.getType()));
        if (!document.getTags().isEmpty()) attributes.add(Attribute.create(Document.TAG_FIELD, document.getTags()));
        if (StringUtils.isNotEmpty(document.getDescription())) {
            attributes.add(Attribute.create(Document.DESCRIPTION_FIELD, document.getDescription()));
        }
        return attributes;
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
