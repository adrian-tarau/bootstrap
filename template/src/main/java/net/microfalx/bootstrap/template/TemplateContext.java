package net.microfalx.bootstrap.template;

import java.util.Map;

/**
 * An interface for a template context.
 * <p>
 * The template context carries variables, which will be made available to
 */
public interface TemplateContext {

    /**
     * Returns a model exposed to the template.
     *
     * @return the model, null if there is no model
     */
    Object getModel();

    /**
     * Returns the names of the variables.
     *
     * @return a non-null instance
     */
    Iterable<String> getNames();

    /**
     * Returns whether the context has a variable with a given name.
     *
     * @param name the name
     * @return {@code true} if exists, {@code false} otherwise
     */
    boolean has(String name);

    /**
     * Returns the variable with a given name.
     *
     * @param name the name of the variable
     * @return the value
     */
    Object get(String name);

    /**
     * Sets a variable into the context.
     *
     * @param name a non-null instance
     */
    Object set(String name, Object value);

    /**
     * Returns a map which wraps the variables available in the context.
     *
     * @return a non-null instance
     */
    Map<String, Object> toMap();

}
