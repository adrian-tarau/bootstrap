package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

/**
 * An interface for a tuple used to display an attribute of an object.
 * <p>
 * The attribute has a name (case-insensitive), a label (to be displayed in UI) and a value.
 */
public interface Attribute extends Nameable, Descriptable, Comparable<Attribute> {

    String SEVERITY = "severity";

    /**
     * Creates a new attribute with a {@code NULL} value.
     *
     * @param name the attribute name
     * @return a non-null instance
     */
    static Attribute create(String name) {
        return new DefaultAttribute(name, null);
    }

    /**
     * Creates a new attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return a non-null instance
     */
    static Attribute create(String name, Object value) {
        return new DefaultAttribute(name, value);
    }

    /**
     * Registers a global attribute priority.
     * <p>
     * Any priority bellow 0 is reserved for system priorities. Any attribute without an assigned priority will be
     * displayed in alphabetical order.
     *
     * @param name     the attribute name
     * @param priority the priority between 0 and 1000, 0 means high priority and 1000 means low priority
     */
    static void registerAttributePriority(String name, int priority) {
        ModelUtils.registerAttributePriority(name, priority);
    }

    /**
     * Returns the label associated with the attribute (to be used in UI).
     *
     * @return a non-null instance
     */
    default String getLabel() {
        return StringUtils.capitalizeWords(getName());
    }

    /**
     * Returns the parent/owner of the attribute.
     *
     * @return a non-null instance if the attribute has a parent, null otherwise
     */
    Attributes<? extends Attribute> getParent();

    /**
     * Returns the value associated with an attribute.
     *
     * @return the value
     */
    Object getValue();

    /**
     * Returns the value associated with an attribute.
     *
     * @return the value
     */
    String asString();

    /**
     * Returns whether the attribute has no value or if the value represents an "empty" object.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns whether the attribute has no value.
     *
     * @return {@code true} if null, {@code false} otherwise
     */
    boolean isNull();
}
