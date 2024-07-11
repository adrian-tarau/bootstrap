package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide alignment rules for fields.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Align {

    /**
     * Returns the alignment of a field.
     *
     * @return a non-null enum
     */
    Type value() default Type.AUTO;

    /**
     * An enum to indicate how to align field values in the grid
     */
    enum Type {

        /**
         * Let the alignment be decided by the data type.
         */
        AUTO,

        /**
         * Align values to the left.
         */
        LEFT,

        /**
         * Align values in the middle.
         */
        CENTER,

        /**
         * Align values to the right.
         */
        RIGHT
    }
}
