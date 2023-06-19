package net.microfalx.bootstrap.web.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provides whether the field is visible
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Visible {

    /**
     * Returns whether the field is visible during browsing.
     *
     * @return {@code true} if visible, {@code false} otherwise
     */
    boolean browse() default true;

    /**
     * Returns whether the field is visible during an add operation.
     *
     * @return {@code true} if visible, {@code false} otherwise
     */
    boolean add() default true;

    /**
     * Returns whether the field is visible during an edit operation.
     *
     * @return {@code true} if visible, {@code false} otherwise
     */
    boolean edit() default true;
}
