package net.microfalx.bootstrap.test.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a field to contain a real object, with other objects injected on creation (or using fields).
 * <p>
 * This annotation is used in combination with {@link ServiceUnitTest} or other similar meta-annotations
 * provided by this framework.
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface Subject {
}
