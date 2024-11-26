package net.microfalx.bootstrap.jdbc.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A collection of rules/strategies to update fields with {@link NaturalIdEntityUpdater}
 */
@Documented
@Retention(RUNTIME)
@Target(value = {TYPE, METHOD, FIELD})
public @interface UpdateStrategy {

    /**
     * Returns whether the field will be used during updates.
     */
    boolean updatable() default false;

    /**
     * Returns a collection of fields to apply the annotation to when the annotation is used with a type.
     *
     * @return a non-null instance
     */
    String[] fieldNames() default {};

}
