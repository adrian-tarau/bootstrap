package net.microfalx.bootstrap.feature.annotation;

import java.lang.annotation.*;

/**
 * An annotation to activate a feature.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(Features.class)
public @interface Feature {

    /**
     * Returns the feature identifier.
     *
     * @return a non-null instance
     */
    String value();
}
