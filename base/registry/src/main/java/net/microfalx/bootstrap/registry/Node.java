package net.microfalx.bootstrap.registry;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * An interface which represents a node in the registry.
 */
public interface Node {

    /**
     * Returns the path of the node.
     *
     * @return a non-null instance
     */
    String getPath();

    /**
     * Returns the parent of this node.
     *
     * @return a non-null instance
     */
    Optional<Node> getParent();

    /**
     * Returns whether the node exists.
     *
     * @return {@code true} if exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Returns weather the node is a leaf (has no children).
     *
     * @return {@code true} if a leaf, {@code false} otherwise
     */
    boolean isLeaf();

    /**
     * Returns the times the node was updated.
     *
     * @return a positive integer
     */
    int getUpdateCount();

    /**
     * Returns the version of the node, which is incremented when the node is updated.
     *
     * @return a positive integer
     */
    int getVersion();

    /**
     * Returns the timestamp when the node was created.
     *
     * @return a non-null instance
     */
    LocalDateTime getCreatedAt();

    /**
     * Returns the timestamp when the node was updated.
     *
     * @return a non-null instance
     */
    LocalDateTime getUpdatedAt();


}
