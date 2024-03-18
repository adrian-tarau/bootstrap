package net.microfalx.bootstrap.content;

import java.io.IOException;

/**
 * A listener to resolve the content.
 */
public interface ContentPersister {

    /**
     * Updates the content.
     *
     * @param content the new content
     * @throws IOException if the content cannot be saved
     */
    void persist(Content content) throws IOException;

    /**
     * Returns whether the listener can resolve the content of the document with a given identifier and URI
     *
     * @param locator the content locator
     * @return {@code true} if the listener can support the document, {@code false} otherwise
     */
    boolean supports(ContentLocator locator);
}
