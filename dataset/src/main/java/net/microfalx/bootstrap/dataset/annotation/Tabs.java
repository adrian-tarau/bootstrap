package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used with models to organize fields in tabs a form.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
}
