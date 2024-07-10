package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide rendering rules in grids for fields.
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Renderable {

    /**
     * Returns whether the display value of the field should be discarded (not rendered).
     * <p>
     * This property is usually used when the display value is not needed since the field will be rendered using
     * additional plug'n'play features.
     *
     * @return {@code true} to disable the display value, {@code false} otherwise
     */
    boolean discard() default false;

    /**
     * Returns the template which will be used to render the markup around the field value.
     *
     * @return the template, em
     */
    String template() default "";
}
