package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Provides indexing capabilities for full text search.
 */
@Service
public class IndexService {

    @Autowired
    private SearchSettings configuration;

    @Autowired
    private ResourceService resourceService;

    /**
     * Indexes a document and commits at the end.
     *
     * @param document the document to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Document document) {
        requireNonNull(document);
        index(Collections.singleton(document));
    }

    /**
     * Indexes a collection of documents and commits at the end.
     *
     * @param documents the collections of documents to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Collection<Document> documents) {
        index(documents, true);
    }

    /**
     * Indexes a collection of documents.
     *
     * @param documents the collections of documents to index
     * @param commit    true - wait to commit in batched documents, false - commit immediately
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Collection<Document> documents, boolean commit) {
        requireNonNull(documents);
    }

    /**
     * Removes a document with from the index.
     * <p/>
     * Item removal is asynchronous, index will be committed later.
     *
     * @param itemId the id of the item to remove
     */
    public void remove(String itemId) {
        requireNonNull(itemId);
    }

    /**
     * Clear the index.
     */
    public void clear() {

    }

}
