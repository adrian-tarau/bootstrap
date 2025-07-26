package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide UI widgit configuraion for data sets fields.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * Returns the type of the component.
     *
     * @return a non-null instance
     */
    Type value() default Type.TEXT_FIELD;

    /**
     * Returns the number of columns (characters) for the component.
     *
     * @return a positive integer, -1 for auto
     */
    int columns() default -1;

    /**
     * Returns the number of rows (characters) for the component.
     *
     * @return a positive integer, -1 for auto
     */
    int rows() default -1;

    /**
     * Returns the separator used between items for {@link Type#TAG}.
     *
     * @return the separator
     */
    char separator() default ',';

    /**
     * An enum for the component type.
     */
    enum Type {
        TEXT_FIELD,
        TEXT_AREA,
        TAG,
        PASSWORD
    }
}
