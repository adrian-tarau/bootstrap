package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Provides indexing capabilities for full text search.
 */
@Service
public class IndexService {

    @Autowired
    private SearchConfiguration configuration;

    @Autowired
    private ResourceService resourceService;

    /**
     * Indexes a collection of documents and commits at the end.
     *
     * @param documents the collections of items to index
     * @throws IndexException if the item cannot be indexed
     */
    public void index(Collection<Document> documents) {

    }

    /**
     * Indexes a collection of documents.
     *
     * @param documents the collections of items to index
     * @param commit    true - wait to commit in batched items, false - commit immediately
     * @throws IndexException if the item cannot be indexed
     */
    public void index(Collection<Document> documents, boolean commit) {

    }

    /**
     * Removes a document with from the index.
     * <p/>
     * Item removal is asynchronous, index will be committed later.
     *
     * @param itemId the id of the item to remove
     */
    public void remove(String itemId) {

    }

    /**
     * Clear the index.
     */
    public void clear() {

    }

}
