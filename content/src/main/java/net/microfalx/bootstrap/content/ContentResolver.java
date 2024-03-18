package net.microfalx.bootstrap.content;

import java.io.IOException;

/**
 * A listener to resolve the content.
 */
public interface ContentResolver {

    /**
     * Resolves a content based on its locator.
     *
     * @param locator the content locator
     * @return the resource, null if it cannot resolve the content
     */
    Content resolve(ContentLocator locator) throws IOException;

    /**
     * Returns whether the listener can resolve the content of the document with a given identifier and URI
     *
     * @param locator the content locator
     * @return {@code true} if the listener can support the document, {@code false} otherwise
     */
    boolean supports(ContentLocator locator);
}
