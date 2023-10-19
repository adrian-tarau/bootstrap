package net.microfalx.bootstrap.model;

import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages a collection of {@link Attribute attributes}.
 */
public interface Attributes<A extends Attribute> extends Iterable<A> {

    /**
     * Creates a default implementation.
     *
     * @param <AA> the attribute type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    static <AA extends Attribute> Attributes<AA> create() {
        return (Attributes<AA>) new DefaultAttributes();
    }

    /**
     * Creates a default implementation.
     *
     * @param <AA> the attribute type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    static <AA extends Attribute> Attributes<AA> empty() {
        return (Attributes<AA>) new EmptyAttributes();
    }

    /**
     * Adds a new attribute, override the previous one if exists.
     *
     * @param attribute the attribute
     * @return the attribute
     */
    A add(A attribute);

    /**
     * Adds a new attribute/parameter, override the previous one if exists.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return the attribute
     */
    A add(String name, Object value);

    /**
     * Adds a new attribute/parameter if a previous attribute does not exist.
     *
     * @param attribute the attribute
     * @return the attribute, the original one if already exists
     */
    A addIfAbsent(A attribute);

    /**
     * Adds a new attribute/parameter if a previous attribute does not exist.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return the attribute, the original one if already exists
     */
    A addIfAbsent(String name, Object value);

    /**
     * Removes an attribute/parameter with a given name.
     *
     * @param name the name
     * @return the attribute, null if it does not exist
     */
    A remove(String name);

    /**
     * Returns an attribute/parameter by its name.
     *
     * @param name the attribute name
     * @return a non-null instance
     */
    A get(String name);

    /**
     * Returns an attribute/parameter by its name.
     *
     * @param name the attribute name
     * @return a non-null instance
     */
    A get(String name, Object defaultValue);

    /**
     * Copies the attributes/parameters from a map.
     *
     * @param values the values
     */
    void copyFrom(Map<String, Object> values);

    /**
     * Copies the attributes/parameters from JSON.
     *
     * @param resource the resource
     * @see #toJson()
     */
    void copyFrom(Resource resource) throws IOException;

    /**
     * Copy the attributes/parameters from a different collection of attributes.
     *
     * @param attributes the attributes
     */
    <AA extends Attribute> void copyFrom(Attributes<AA> attributes);

    /**
     * Returns whether the attributes/parameters cannot be changed.
     *
     * @return {@code true} if read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Returns whether there are no attributes/parameters.
     *
     * @return {@code} true if empty, {@code false}
     */
    boolean isEmpty();

    /**
     * Returns the attributes/parameters as a read-only map.
     *
     * @return a non-null instance
     */
    Map<String, A> toMap();

    /**
     * Returns the attributes/parameter values in the order of the insertion.
     *
     * This is equivalent to {@link #toMap()#values()}.
     * @return a non-null instance
     */
    Collection<Object> toValues();

    /**
     * Returns the attributes/parameters as a collection ordered by priority.
     *
     * @return a non-null instance
     * @see Attribute#registerAttributePriority(String, int)
     */
    Collection<A> toCollection();

    /**
     * Returns a subset of the attributes/parameters as a collection ordered by priority.
     *
     * @param maximumCount the maximum number of entries returned
     * @return a non-null instance
     * @see Attribute#registerAttributePriority(String, int)
     */
    Collection<A> toCollection(int maximumCount);

    /**
     * Returns a subset of the attributes/parameters as a collection ordered by priority.
     *
     * @param maximumCount the maximum number of entries returned
     * @return a non-null instance
     * @see Attribute#registerAttributePriority(String, int)
     */
    Collection<A> toCollection(int maximumCount, Function<A, Boolean> filter);

    /**
     * Converts the attributes/parameters to JSON.
     *
     * @return a non-null instance
     */
    Resource toJson();
}
