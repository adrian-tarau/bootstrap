package net.microfalx.bootstrap.security.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides an action for a user audit.
 */
@Documented
@Retention(RUNTIME)
@Target(value = {TYPE, METHOD})
@Inherited
public @interface Action {

    /**
     * Returns the action name.
     *
     * @return a non-null instance
     */
    String value();
}
