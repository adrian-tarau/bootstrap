package net.microfalx.bootstrap.test.extension;

import org.atteo.classindex.IndexSubclasses;

/**
 * An interface used to provide plug'n'play support to create objects during the setup of a unit test.
 */
@IndexSubclasses
public interface ComponentCreator<T> {

    /**
     * Returns the type object it can create.
     *
     * @return the type
     */
    Class<T> getType();

    /**
     * Creates an instance of the component.
     *
     * @return a non-null instance
     */
    T create(Session session);

}
