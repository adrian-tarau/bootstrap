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
     * The field is rendered as an action (link) with using the text of the field or a custom icon.
     * <p>
     * The action is translated into an event.
     *
     * @return the action (event), empty if there is no action
     */
    String action() default "";

    /**
     * Returns the icon (class) associated with the action.
     *
     * @return the icon or null if there is no icon
     */
    String icon() default "";

    /**
     * Returns the template which will be used to render the markup around the field value.
     *
     * @return the template, em
     */
    String template() default "";
}
