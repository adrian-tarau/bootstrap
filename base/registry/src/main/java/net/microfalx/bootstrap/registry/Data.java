package net.microfalx.bootstrap.registry;

/**
 * An interface which represents the data stored in a node in registry.
 */
public interface Data {

    /**
     * Creates a new instance for a given path in registry
     *
     * @param path the path
     * @return a non-null instance
     */
    static Data create(String path) {
        return new DataImpl(path);
    }

    /**
     * Returns the node information associated with the data.
     *
     * @return a non-null instance
     */
    Node getNode();

    /**
     * Returns whether there is data stored a node in the registry at the given path.
     *
     * @return {@code true} if exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Returns the value associated with this data.
     *
     * @param <T> the type of result
     * @return the value, null if not exists
     */
    <T> T get();

    /**
     * Changes the value associated with this data.
     *
     * @param value the value
     * @param <T>   the type of value
     */
    <T> void set(T value);

    /**
     * Changes an attribute associated with this data.
     *
     * @param name  the name of the attribute
     * @param value the value
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the value of the given attribute.
     *
     * @param name the name of the attribute
     * @param <T>  the type of the attribute
     * @return the value, null if it does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Returns the string value of the given attribute.
     *
     * @param name         the name of the attribute
     * @param defaultValue the default value
     * @return the string, default is missing
     */
    String getAttribute(String name, String defaultValue);

    /**
     * Returns the int value of the given attribute.
     *
     * @param name         the name of the attribute
     * @param defaultValue the default value
     * @return the int, default is missing
     */
    int getAttribute(String name, int defaultValue);

    /**
     * Returns the long value of the given attribute.
     *
     * @param name         the name of the attribute
     * @param defaultValue the default value
     * @return the long, default is missing
     */
    long getAttribute(String name, long defaultValue);

}
