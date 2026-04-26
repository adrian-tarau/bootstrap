package net.microfalx.bootstrap.configuration.annotation;

import java.lang.annotation.*;

/**
 * An annotation which binds the application configuration ({@link net.microfalx.bootstrap.configuration.Configuration}
 * to an interface.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationMapping {

    /**
     * Returns the binding prefix.
     *
     * @return a non-null instance
     */
    String prefix();
}
