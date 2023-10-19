package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IContext;

import java.io.IOException;
import java.net.URI;

/**
 * Template utilities around {@link net.microfalx.resource.Resource}.
 * <p>
 * All methods return a safe value instead of failing.
 */
@SuppressWarnings("unused")
public class ResourceTool extends AbstractTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTool.class);

    public ResourceTool(IContext context) {
        super(context);
    }

    /**
     * Returns the resource as a text.
     *
     * @param uri the URI as string
     * @return the text, error message
     */
    public String load(String uri) {
        URI auri;
        try {
            auri = URI.create(uri);
        } catch (Exception e) {
            LOGGER.error("Invalid URI '" + uri + "', root cause: " + e.getMessage());
            return "Invalid URI: " + uri;
        }
        return load(auri);
    }

    /**
     * Returns the resource as a text.
     *
     * @param uri the URI
     * @return the text, error message
     */
    public String load(URI uri) {
        Resource resource;
        try {
            resource = ResourceFactory.resolve(uri);
        } catch (Exception e) {
            LOGGER.error("Failed to locate resource '" + uri + "'", e);
            return "Resource '" + uri + "' is unavailable";
        }
        return load(resource);
    }

    /**
     * Returns the resource as a text.
     *
     * @param resource the resource
     * @return the text, error message
     */
    public String load(Resource resource) {
        try {
            return resource.loadAsString();
        } catch (IOException e) {
            LOGGER.error("Failed to retrieve resource '" + resource.toURI() + "'", e);
            return "Resource '" + resource.toURI() + "' is unavailable";
        }
    }
}
