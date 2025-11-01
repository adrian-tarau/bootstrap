package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.resource.Resource;

import java.util.Collection;

/**
 * A class which can parse a SQL script and extract individual statements.
 */
public interface Script {

    /**
     * The schema this script is intended for.
     *
     * @return a non-null instance
     */
    Schema getSchema();

    /**
     * Returns the resource representing the script.
     *
     * @return a non-null instance
     */
    Resource getResource();

    /**
     * Returns the statements contained in this script.
     *
     * @return a non-null instance
     */
    Collection<Query> getQueries();


}
