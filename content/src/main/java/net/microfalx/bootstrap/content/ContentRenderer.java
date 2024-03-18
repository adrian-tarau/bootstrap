package net.microfalx.bootstrap.content;

import net.microfalx.resource.Resource;

import java.io.IOException;

/**
 * A listener which provides various formats of the content to be viewed or edited.
 */
public interface ContentRenderer {

    /**
     * Prepares a pretty print (human readable layout) for a given content.
     *
     * @param resource the resource
     * @return the resource with
     */
    default Resource prettyPrint(Resource resource) throws IOException {
        return resource;
    }

    /**
     * Renders the content fully or partially based on the page information in
     * a format that's suitable for viewing the content.
     *
     * @param content the content
     * @return the resource with
     */
    default Resource view(Content content) throws IOException {
        return content.getResource();
    }

    /**
     * Renders the content fully or partially based on the page information in
     * a format that's suitable for changing the content.
     *
     * @param content the content
     * @return the resource with
     */
    default Resource edit(Content content) throws IOException {
        return content.getResource();
    }

    /**
     * Returns whether the listener can resolve the content of the document with a given identifier and URI
     *
     * @param content the content
     * @return {@code true} if the listener can support the content, {@code false} otherwise
     */
    boolean supports(Content content);
}
