package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to indicate which fields are searchable just by typing any unqualified filter expression.
 * <p>
 * By default all {@link net.microfalx.bootstrap.model.Field.DataType#STRING} fields are searchable.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Searchable {

    /**
     * Returns whether the annotated field is searchable.
     *
     * @return {@code true} if searchable, {@code false} otherwise
     */
    boolean value() default false;
}
