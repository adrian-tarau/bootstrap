package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used with models to organize fields in tabs a form.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Tabs {

    /**
     * Returns whether tabs should be created out of label groups.
     *
     * @return {@code true} if label groups should be used to create tabs, {@code false} otherwise
     */
    boolean useLabelAsGroup() default true;

    /**
     * Returns the name of the default tab.
     * <p>
     * Any field that does not belong to a specific tab will be placed in the default tab.
     *
     * @return the name of the tab
     */
    String defaultTab() default "General";

    /**
     * Returns an array of field names that should be displayed in a tab called "Attributes".
     *
     * @return the attributes
     */
    String[] attributes() default {};

    /**
     * Returns the per-field tab overrides
     *
     * @return an array of tabs to override the default tab behavior
     */
    Tab[] fields() default {};


}
