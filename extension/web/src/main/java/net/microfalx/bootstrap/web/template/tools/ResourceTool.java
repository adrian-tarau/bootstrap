package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import java.io.IOException;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseName;
import static net.microfalx.lang.UriUtils.parseUri;

/**
 * Template utilities around {@link net.microfalx.resource.Resource}.
 * <p>
 * All methods return a safe value instead of failing.
 */
@SuppressWarnings("unused")
public class ResourceTool extends AbstractTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTool.class);

    public ResourceTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    /**
     * Returns the resource as a text.
     *
     * @param uri the URI as string
     * @return the text, error message
     */
    public String load(String uri) {
        requireNonNull(uri);
        return load(parseUri(uri));
    }

    /**
     * Returns the resource as a text.
     *
     * @param uri the URI
     * @return the text, error message
     */
    public String load(URI uri) {
        requireNonNull(uri);
        Resource resource = resolve(uri);
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
            if (resource == null || !resource.exists()) return null;
            return resource.loadAsString();
        } catch (IOException e) {
            LOGGER.error("Failed to retrieve resource '{}', root cause: {}", resource.toURI(), getRootCauseName(e));
            return "Resource '" + resource.toURI() + "' is unavailable";
        }
    }

    /**
     * Returns the resource for a URI.
     *
     * @param uri the URI
     * @return the text, error message
     */
    public Resource resolve(String uri) {
        return resolve(parseUri(uri));
    }

    /**
     * Returns the resource for a URI.
     *
     * @param uri the URI
     * @return the text, error message
     */
    public Resource resolve(URI uri) {
        Resource resource;
        try {
            return ResourceFactory.resolve(uri);
        } catch (Exception e) {
            LOGGER.error("Failed to locate resource '{}', root cause {}", uri, getRootCauseName(e));
            return Resource.NULL;
        }
    }
}
