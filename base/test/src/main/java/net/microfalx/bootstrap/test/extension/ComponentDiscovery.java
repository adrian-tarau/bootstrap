package net.microfalx.bootstrap.test.extension;

import org.atteo.classindex.IndexSubclasses;

/**
 * An interface used to provide new component types to be registered for all tests.
 */
@IndexSubclasses
public interface ComponentDiscovery {

    /**
     * Returns a collection of components/services to be registered.
     *
     * @return a non-null instance
     */
    Class<?>[] value();
}
