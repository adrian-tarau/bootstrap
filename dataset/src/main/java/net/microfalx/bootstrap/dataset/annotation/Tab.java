package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to identify a tab.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tab {

    /**
     * Returns the label of the tab.
     * <p>
     * If not specified, the label will be derived from the identifier.
     *
     * @return the label of the tab
     */
    String label();
}
