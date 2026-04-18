package net.microfalx.bootstrap.test.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to prepare mocks and subjects which are not actively used/verified
 * but they will be used by the object under test (the subject).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Prepare {

    /**
     * A collection of classes to be mocked.
     * <p>
     * If there are answers available for a given type ( using {@link AnswerFor}) they will be used,
     * otherwise a safe mock will be created.
     *
     * @return a non-null instance
     */
    Class<?>[] mocks() default {};

    /**
     * A collection of classes to be instantiated and be available to be injected in current
     * (test class) subjects.
     *
     * @return a non-null instance
     */
    Class<?>[] subjects() default {};
}
