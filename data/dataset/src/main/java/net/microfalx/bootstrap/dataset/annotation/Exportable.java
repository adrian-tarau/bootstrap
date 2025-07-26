package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to indicate which fields are exportable.
 * <p>
 * By default all ids and visible fields are exportable.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Exportable {

    /**
     * Returns whether the annotated field is exportable.
     *
     * @return {@code true} if searchable, {@code false} otherwise
     */
    boolean value() default false;
}
