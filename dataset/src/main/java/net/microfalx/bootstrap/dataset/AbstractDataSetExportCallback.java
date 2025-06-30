package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.Initializable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.UriUtils.parseUri;

/**
 * Base class for data set export callbacks.
 */
public abstract class AbstractDataSetExportCallback<M, F extends Field<M>, ID> extends ApplicationContextSupport
        implements DataSetExportCallback<M, F, ID>, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetExportCallback.class);

    @Override
    public void initialize(Object... context) {
        // subclasses can override this method to perform initialization
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
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns the resource for a URI.
     *
     * @param uri the URI as string
     * @return the text, error message
     */
    protected final Resource resolve(String uri) {
        if (StringUtils.isEmpty(uri)) return Resource.memory();
        return resolve(parseUri(uri));
    }

    /**
     * Returns the resource for a URI.
     *
     * @param uri the URI
     * @return the text, error message
     */
    protected final Resource resolve(URI uri) {
        try {
            return ResourceFactory.resolve(uri);
        } catch (Exception e) {
            return rethrowExceptionAndReturn(e);
        }
    }
}
