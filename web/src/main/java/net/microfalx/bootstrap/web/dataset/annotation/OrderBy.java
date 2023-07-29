package net.microfalx.bootstrap.web.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide default sorting for data sets.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrderBy {

    /**
     * Returns the direction of sorting
     *
     * @return a non-null instance
     */
    Direction value() default Direction.ASC;

    /**
     * Enumeration for sort directions.
     */
    enum Direction {
        /**
         * A value for ascending order.
         */
        ASC,

        /**
         * A value for descending order.
         */
        DESC;
    }
}
