package net.microfalx.bootstrap.configuration;

/**
 * A configuration subset is a part of the configuration tree which has a parent and a title.
 * <p>
 * It is used to group configurations from user interface perspective.
 */
public interface Subset extends Configuration {

    /**
     * Returns the title associated with this configuration subset (group).
     *
     * @return a non-null instance
     */
    String getTitle();

    /**
     * Returns the configuration prefix associated with this subset.
     *
     * @return a non-null instance, EMPTY if root
     */
    String getPrefix();
}
