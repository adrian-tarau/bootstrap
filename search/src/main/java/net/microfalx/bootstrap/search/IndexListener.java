package net.microfalx.bootstrap.search;

import java.util.Collection;

/**
 * A listener picked by {@link IndexService}.
 */
public interface IndexListener {

    /**
     * Invoked after documents are indexed.
     *
     * @param documents a collection of indexed documents
     */
    void indexed(Collection<Document> documents);
}
