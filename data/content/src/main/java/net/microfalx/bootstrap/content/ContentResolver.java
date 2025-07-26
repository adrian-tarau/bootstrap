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
     * @throws IOException I/O exception received from the resolver
     */
    Content resolve(ContentLocator locator) throws IOException;

    /**
     * Intercepts the content.
     * <p>
     * The content can be intercepted to add more attributes, change the content, etc.
     *
     * @param content the content
     * @return the new content
     * @throws IOException I/O exception received from the resolver
     */
    default Content intercept(Content content) throws IOException {
        return content;
    }

    /**
     * Returns whether the listener can resolve the content of the document with a given identifier and URI
     *
     * @param locator the content locator
     * @return {@code true} if the listener can support the document, {@code false} otherwise
     */
    boolean supports(ContentLocator locator);
}
