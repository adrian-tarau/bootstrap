package net.microfalx.bootstrap.web.application.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used to select the active theme for a view.
 */
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, TYPE})
@Documented
@Inherited
public @interface Theme {

    /**
     * Returns the identifier of the theme.
     *
     * @return a non-null instance
     */
    String value();

    /**
     * Returns the  mode for the theme.
     *
     * @return a non-null instance
     */
    Mode mode() default Mode.AUTO;

    /**
     * An enum for the modes of a theme.
     */
    enum Mode {

        /**
         * Use the light mode.
         */
        LIGHT,

        /**
         * Use the dark mode.
         */
        DARK,

        /**
         * Use the system mode
         */
        AUTO;
    }

}
