package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide information about a data set
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSet {

    /**
     * Returns the model for the data set record.
     *
     * @return the model class, Object is not set
     */
    Class<?> model() default Object.class;

    /**
     * Returns whether the data set will have virtual scrolling.
     *
     * @return {@code true} for virtual scrolling, {@code false} otherwise
     */
    boolean virtual() default true;

    /**
     * Returns the default page size.
     *
     * @return a non-null instance
     */
    int pageSize() default 30;
}
