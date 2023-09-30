package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.FormatterUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.search.SearchUtils.isNumericField;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.TimeUtils.toMillis;

/**
 * A service used to execute full text searches.
 */
@Service
public class SearchService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    static Metrics METRICS = Metrics.of("search");


    @Autowired
    private SearchProperties searchProperties;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private I18nService i18nService;

    private volatile AsyncTaskExecutor taskExecutor;

    private final Object lock = new Object();
    private volatile SearchHolder searchHolder;

    private final Map<String, String> attributeClasses = new ConcurrentHashMap<>();
    private final Collection<SearchListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, String> labels = new ConcurrentHashMap<>();
    private final Map<String, String> description = new ConcurrentHashMap<>();

    /**
     * Returns the executor used by the search service.
     *
     * @return a non-null instance
     */
    public AsyncTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) taskExecutor = TaskExecutorFactory.create("search").createExecutor();
        return taskExecutor;
    }

    /**
     * Finds one document by id.
     *
     * @param id the document identifier.
     * @return the document, null if such
     */
    public Document find(String id) {
        requireNonNull(id);
        LOGGER.info("Searching for document with identifier  '" + id + "'");
        try {
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.registerListener(new RetryListener() {
                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    LOGGER.info("Find failure " + throwable.getMessage());
                    releaseSearchHolder();
                }
            });
            return retryTemplate.execute((RetryCallback<Document, Exception>) context -> doFind(id));
        } catch (Exception e) {
            throw new SearchException("Exception during find for '" + id + "'", e);
        }
    }

    /**
     * Search the index for all items matching the query.
     *
     * @param query the query information
     * @return matching items
     * @throws SearchException if the query cannot be executed
     */
    public SearchResult search(SearchQuery query) {
        requireNonNull(query);
        try {
            final Query parsedQuery = createQuery(query);
            final SearchResult result = new SearchResult(query);
            result.setRewriteQuery(parsedQuery.toString());
            LOGGER.info("Searching with query '" + query.getDescription() + "', normalized query: '" + getNormalizedQuery(query) + "', parsed query: '" + parsedQuery
                    + "', auto-wildcard: " + query.isAutoWildcard() + ", leading wildcard: " + query.isAllowLeadingWildcard());
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.registerListener(new RetryListener() {
                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    LOGGER.info("Searcher failure " + throwable.getMessage());
                    releaseSearchHolder();
                }
            });
            return retryTemplate.execute((RetryCallback<SearchResult, Exception>) context -> {
                doSearch(parsedQuery, query, result);
                return result;
            });
        } catch (ParseException e) {
            throw new SearchException("Failed to parse query string: " + query, e);
        } catch (Exception e) {
            throw new SearchException("Exception during search for : " + query, e);
        }
    }

    /**
     * Returns the description of the item with searched terms highlighted.
     *
     * @param query  the query
     * @param itemId the item id to highlight
     * @return the text
     */
    public String getHighlightedText(SearchQuery query, String itemId) {
        return null;
    }

    /**
     * Returns the label associated with a field.
     *
     * @param field the field
     * @return the label
     */
    public String getLabel(String field) {
        requireNonNull(field);
        String label = labels.get(field);
        if (label != null) return label;
        for (SearchListener listener : listeners) {
            label = listener.getLabel(field);
            if (StringUtils.isNotEmpty(label)) break;
        }
        if (label == null) label = getI18n("label");
        if (label == null) label = StringUtils.capitalizeWords(field);
        labels.put(field, label);
        return label;
    }

    /**
     * Returns the description associated with a field.
     *
     * @param field the field
     * @return the label
     */
    public String getDescription(String field) {
        requireNonNull(field);
        String label = labels.get(field);
        if (label != null) return label;
        for (SearchListener listener : listeners) {
            label = listener.getDescription(field);
            if (StringUtils.isNotEmpty(label)) break;
        }
        if (label == null) label = getI18n("description");
        labels.put(field, label);
        return label;
    }

    /**
     * Returns whether the attribute will be displayed in the search result.
     *
     * @param document  the search engine document
     * @param attribute the attribute
     * @return {@code true} if accepted, {@code false} otherwise
     */
    public boolean accept(Document document, Attribute attribute) {
        requireNonNull(document);
        requireNonNull(attribute);
        for (SearchListener listener : listeners) {
            if (!listener.accept(document, attribute)) return false;
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initListeners();
        initTaskExecutor();
    }

    private String getNormalizedQuery(SearchQuery query) {
        String normalizedQuery = SearchUtils.normalizeQuery(query.getQuery(), query.isAutoWildcard(), query.isAllowLeadingWildcard()).trim();
        if (isNotEmpty(query.getFilter())) {
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(normalizedQuery).append(") AND (").append(query.getFilter()).append(")");
            normalizedQuery = builder.toString();
        }
        return normalizedQuery;
    }

    private Query createQuery(SearchQuery query) throws ParseException {
        final String normalizedQuery = getNormalizedQuery(query);
        final QueryParser queryParser = createQueryParser();
        queryParser.setAllowLeadingWildcard(query.isAllowLeadingWildcard() && searchProperties.isAllowLeadingWildcard());
        Query parsedQuery = null;
        if (StringUtils.isNotEmpty(normalizedQuery)) parsedQuery = queryParser.parse(normalizedQuery);
        Query timeQuery = null;
        if (query.getStartTime() != null) {
            timeQuery = LongPoint.newRangeQuery(Document.MODIFIED_AT_FIELD, toMillis(query.getStartTime()), toMillis(query.getEndTime()));
        }
        if (parsedQuery == null && timeQuery == null) {
            return new MatchAllDocsQuery();
        } else if (parsedQuery != null && timeQuery == null) {
            return parsedQuery;
        } else if (parsedQuery == null) {
            return timeQuery;
        } else {
            return new BooleanQuery.Builder().add(parsedQuery, BooleanClause.Occur.MUST)
                    .add(timeQuery, BooleanClause.Occur.MUST).build();
        }
    }

    private Document doFind(String id) throws IOException {
        final IndexSearcher indexSearcher = getIndexSearcher();
        Document translatedDocument = null;
        try (Timer ignored = METRICS.startTimer("find")) {
            TermQuery query = new TermQuery(new Term(Document.ID_FIELD, id));
            TopDocs topDocs = indexSearcher.search(query, 1);
            DocumentMapper documentMapper = new DocumentMapper();
            if (topDocs.scoreDocs.length > 0) {
                org.apache.lucene.document.Document document = indexSearcher.doc(topDocs.scoreDocs[0].doc);
                translatedDocument = documentMapper.read(document);
            }
        }
        if (translatedDocument != null)
            LOGGER.info("Found document with identifier " + id + ", name " + translatedDocument.getName());
        return translatedDocument;
    }

    @SuppressWarnings("resource")
    private void doSearch(Query luceneQuery, SearchQuery searchQuery, SearchResult result) throws IOException {
        final IndexSearcher indexSearcher = getIndexSearcher();
        final List<Document> items = new ArrayList<>();
        final Sort sort = createSort(searchQuery);

        TopDocs topDocs;
        try (Timer ignored = METRICS.startTimer("search")) {
            topDocs = indexSearcher.search(luceneQuery, searchQuery.getStart() + searchQuery.getLimit(), sort);
            int startIndex = searchQuery.getStart();
            int counter = searchQuery.getLimit();
            DocumentMapper documentMapper = new DocumentMapper();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                if (startIndex-- > 0) continue;
                org.apache.lucene.document.Document document = indexSearcher.doc(scoreDoc.doc);
                Document translatedDocument = documentMapper.read(document);
                translatedDocument.setRelevance(Float.isNaN(scoreDoc.score) ? Document.NO_RELEVANCE : scoreDoc.score);
                items.add(translatedDocument);
                if (counter-- == 0) break;
            }
        }
        LOGGER.info("Found " + topDocs.totalHits + " total hit(s), took " + FormatterUtils.formatNumber(Timer.last().getDuration()));

        result.setDocuments(items);
        result.setTotalHits(topDocs.totalHits.value);
    }

    private Sort createSort(SearchQuery searchQuery) {
        SearchQuery.Sort querySort = searchQuery.getSort();
        if (querySort.getType() == SearchQuery.Sort.Type.FIELD) {
            String field = querySort.getField();
            field = isNumericField(field) ? field + Document.SORTED_SUFFIX_FIELD : field;
            SortField sortField = new SortedNumericSortField(field, SortField.Type.LONG, querySort.isReversed());
            return new Sort(sortField);
        } else if (querySort.getType() == SearchQuery.Sort.Type.RELEVANCE) {
            return Sort.RELEVANCE;
        } else if (querySort.getType() == SearchQuery.Sort.Type.INDEX_ORDER) {
            return Sort.INDEXORDER;
        } else {
            throw new SearchException("Unknown sort type: " + querySort.getType());
        }
    }

    private IndexSearcher getIndexSearcher() {
        return getSearchHolder(false).getIndexSearcher();
    }

    /**
     * Returns an structure which carries an {@link org.apache.lucene.search.IndexSearcher} and associated
     * objects.
     *
     * @param reopen <code>true</code> to re-open the index reader, <code>false</code> to reuse the index reader
     * @return a non-null instance
     * @throws SearchException if the index cannot be opened
     */
    private SearchHolder getSearchHolder(boolean reopen) {
        synchronized (lock) {
            if (searchHolder == null || reopen) {
                if (searchHolder != null) {
                    releaseSearchHolder();
                }
                LOGGER.debug("Open searcher");
                try (Timer ignored = METRICS.startTimer("open_searcher")) {
                    try {
                        searchHolder = new SearchHolder();
                    } catch (IOException e) {
                        return ExceptionUtils.throwException(e);
                    }
                }
            }
            return searchHolder;
        }
    }

    private void releaseSearchHolder() {
        LOGGER.debug("Release  searcher");
        synchronized (lock) {
            if (searchHolder != null) {
                try (Timer ignored = METRICS.startTimer("release_searcher")) {
                    searchHolder.release();
                }
                searchHolder = null;
            }
        }
    }

    private QueryParser createQueryParser() {
        final QueryParser queryParser = Analyzers.createQueryParser(SearchUtils.DEFAULT_FIELD);
        queryParser.setAllowLeadingWildcard(searchProperties.isAllowLeadingWildcard());
        return queryParser;
    }

    private File getIndexDirectory() {
        Resource resource = resourceService.getPersisted("index");
        return ((FileResource) resource).getFile();
    }

    private void initListeners() {
        LOGGER.info("Register listeners");
        Collection<SearchListener> discoveredListeners = ClassUtils.resolveProviderInstances(SearchListener.class);
        for (SearchListener discoveredListener : discoveredListeners) {
            LOGGER.info(" - " + ClassUtils.getName(discoveredListener));
            this.listeners.add(discoveredListener);
        }
    }

    private void initTaskExecutor() {
        taskExecutor = TaskExecutorFactory.create().setSuffix("search").createExecutor();
    }

    private String getI18n(String suffix) {
        return i18nService.getText("search.field." + suffix);
    }

    /**
     * Holds a {@link org.apache.lucene.index.IndexWriter} and any required structures.
     */
    private class SearchHolder {

        private final IndexSearcher indexSearcher;
        private final IndexReader indexReader;
        private final Directory directory;

        private final AtomicBoolean stale = new AtomicBoolean();
        private final AtomicInteger useCount = new AtomicInteger(0);

        private SearchHolder() throws IOException {
            File indexDirectory = getIndexDirectory();
            directory = new NIOFSDirectory(indexDirectory.toPath(), NativeFSLockFactory.getDefault());
            try (Timer ignored = METRICS.startTimer("open_reader")) {
                indexReader = DirectoryReader.open(directory);
            }
            try (Timer ignored = METRICS.startTimer("open_searcher")) {
                indexSearcher = new IndexSearcher(indexReader);
            }
        }

        public IndexReader getIndexReader() {
            return indexReader;
        }

        public IndexSearcher getIndexSearcher() {
            return indexSearcher;
        }

        public synchronized void release() {
            try {
                indexReader.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close index reader", e);
            }
            try {
                if (directory != null) directory.close();
            } catch (IOException e) {
                LOGGER.error("Failed to rollback index", e);
            }
        }
    }
}
