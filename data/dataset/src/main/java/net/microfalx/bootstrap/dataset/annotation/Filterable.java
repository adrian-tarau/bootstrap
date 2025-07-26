package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to indicate which fields are filterable (will be added to the filter when clicked on
 * the column in the grid).
 * <p>
 * By default all {@link net.microfalx.bootstrap.model.Field.DataType#STRING} fields are filterable.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Filterable {

    /**
     * Returns whether the annotated field is filterable.
     *
     * @return {@code true} if searchable, {@code false} otherwise
     */
    boolean value() default false;

    /**
     * Returns the field name actually used in the filter instead of the annotated field.
     *
     * @return {@code the field name} if different from annotated field, {@code empty} to use annotated field
     */
    String name() default "";
}
