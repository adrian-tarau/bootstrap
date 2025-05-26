package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to identify a tab.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tab {

    /**
     * Returns the field name associated with this tab.
     * <p>
     * If not specified, the field name will be derived from the annotated field or method.
     *
     * @return the field name associated with this tab
     */
    String fieldName() default "";

    /**
     * Returns the label of the tab.
     * <p>
     * If not specified, the label will be derived from the identifier.
     *
     * @return the label of the tab
     */
    String label();
}
