package net.microfalx.bootstrap.content;

import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;

import java.io.IOException;

/**
 * A provider which simply tries to resolve the content based on URI.
 */
@Provider
@Order(Order.AFTER)
public final class DefaultContentResolver implements ContentResolver {

    @Override
    public Content resolve(ContentLocator locator) throws IOException {
        Resource resource = ResourceFactory.resolve(locator.getUri());
        return Content.create(locator, resource);
    }

    @Override
    public boolean supports(ContentLocator locator) {
        return true;
    }
}
