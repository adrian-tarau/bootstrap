package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to indicate which fields are sortable.
 * <p>
 * By default all fields are sortable.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sortable {

    /**
     * Returns whether the annotated field is searchable.
     *
     * @return {@code true} if searchable, {@code false} otherwise
     */
    boolean value() default false;
}
