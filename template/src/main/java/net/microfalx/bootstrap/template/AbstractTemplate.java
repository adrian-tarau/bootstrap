package net.microfalx.bootstrap.template;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.resource.Resource;

/**
 * Base class for all templates
 */
public abstract class AbstractTemplate implements Template {

    private final Resource resource;

    public AbstractTemplate(Resource resource) {
        ArgumentUtils.requireNonNull(resource);
        this.resource = resource;
    }

    @Override
    public final Resource getResource() {
        return resource;
    }
}
