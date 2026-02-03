package net.microfalx.bootstrap.feature.annotation;

import java.lang.annotation.*;

/**
 * An annotation to specify multiple features.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Features {

    /**
     * Returns the list of features.
     *
     * @return a non-null instance
     */
    Feature[] value();
}
