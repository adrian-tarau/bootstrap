package net.microfalx.bootstrap.registry;

import java.util.Collection;
import java.util.Optional;

/**
 * An interface representing the storage used by {@link Registry} to store data.
 */
public interface Storage {

    /**
     * Creates an in-memory storage implementation
     *
     * @return a non-null instance
     */
    static Storage create() {
        return new MemoryStorage();
    }

    /**
     * Returns whether the storage is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Returns the children of a given path.
     *
     * @param path      the path
     * @param recursive {@code true if the children should be returned recursively, {@code false} otherwise}
     * @return a non-null instance
     */
    Collection<Node> getChildren(String path, boolean recursive);

    /**
     * Loads a node by a given path
     *
     * @param path the path
     * @return a non-null instance
     */
    Optional<Node> getNode(String path);

    /**
     * Returns whether there is a node in the registry at the given path.
     *
     * @param path the path
     * @return {@code true} if exists, {@code false} otherwise
     */
    boolean exists(String path);

    /**
     * Returns the data available in the registry at a given path.
     *
     * @param path the path
     * @return the data, null if no data is available
     */
    byte[] get(String path);

    /**
     * Changes the data in the registry at a given path.
     *
     * @param path the path
     * @param data the data
     */
    void put(String path, byte[] data);

    /**
     * Changes the data in the registry at a given path.
     *
     * @param path the path
     * @param data the data
     */
    void put(String path, byte[] data, int version);

    /**
     * Removes the data and the node under a given path.
     *
     * @param path the path
     */
    void remove(String path);
}
