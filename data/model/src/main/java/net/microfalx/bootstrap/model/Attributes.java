package net.microfalx.bootstrap.model;

import io.micrometer.common.lang.Nullable;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Manages a collection of {@link Attribute attributes}.
 */
public interface Attributes<A extends Attribute> extends Iterable<A> {

    /**
     * Creates attributes with a default implementation.
     *
     * @param <AA> the attribute type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    static <AA extends Attribute> Attributes<AA> create() {
        return (Attributes<AA>) new DefaultAttributes();
    }

    /**
     * Creates mutable attributes with default implementation and copies the attributes over.
     *
     * @param <AA>       the attribute type
     * @param attributes the source for the new instance
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    static <AA extends Attribute> Attributes<AA> create(@Nullable Attributes<?> attributes) {
        return create(attributes, false);
    }

    /**
     * Creates attributes with default implementation and copies the attributes over.
     *
     * @param <AA>       the attribute type
     * @param attributes the source for the new instance
     * @param readOnly   {@code true} to create immutable attributes, {@code false} to be mutable
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    static <AA extends Attribute> Attributes<AA> create(@Nullable Attributes<?> attributes, boolean readOnly) {
        DefaultAttributes result = new DefaultAttributes();
        if (attributes != null) result.copyFrom(attributes);
        result.setReadOnly(readOnly);
        return (Attributes<AA>) result;
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
    Attributes<A> copyFrom(Map<String, Object> values);

    /**
     * Copies the attributes/parameters from a map.
     *
     * @param values the values
     */
    Attributes<A> copyFrom(Map<String, Object> values, Function<String, Boolean> filter);

    /**
     * Copies the attributes/parameters from JSON/Properties.
     *
     * @param resource the resource
     * @see #toJson()
     */
    Attributes<A> copyFrom(Resource resource) throws IOException;

    /**
     * Copy the attributes/parameters from a different collection of attributes.
     *
     * @param attributes the attributes, can be null
     */
    <AA extends Attribute> Attributes<A> copyFrom(Attributes<AA> attributes);

    /**
     * Copy the attributes/parameters from a different collection of attributes.
     *
     * @param attributes the attributes
     * @param filter the filter to apply, can be null to include all
     */
    <AA extends Attribute> Attributes<A> copyFrom(Attributes<AA> attributes, Function<AA, Boolean> filter);

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
     * Returns the number of attributes.
     *
     * @return a positive integer
     */
    int size();

    /**
     * Returns the attribute names.
     * @return a non-null instance
     */
    Set<String> getNames();

    /**
     * Returns the attributes/parameters as a read-only map.
     *
     * @return a non-null instance
     */
    Map<String, A> toMap();

    /**
     * Returns the attributes/parameter values in the order of the insertion.
     * <p>
     * This is equivalent to {@link #toMap()#values()}.
     *
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

    /**
     * Converts the attributes/parameters to properties.
     *
     * @return a non-null instance
     */
    Resource toProperties();

    /**
     * Replaces the variables in the given text.
     * <p>
     * Variables can be accessed by using the placeholder <code>${name}</code>. Parameters are case-insensitive.
     *
     * @param text the text with variables
     * @return the text with all variables replaced
     */
    String replaceVariables(String text);
}
