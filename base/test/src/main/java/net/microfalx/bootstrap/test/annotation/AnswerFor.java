package net.microfalx.bootstrap.test.annotation;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.*;

/**
 * An annotation used to auto-discover answers to mock objects.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@IndexAnnotated
public @interface AnswerFor {

    /**
     * Returns the class for which the answer is provided.
     *
     * @return a non-null instance
     */
    Class<?> value();
}
