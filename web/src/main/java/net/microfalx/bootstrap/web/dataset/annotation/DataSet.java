package net.microfalx.bootstrap.web.dataset.annotation;

import org.springframework.data.repository.Repository;

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
     * Returns a repository used as a data source for the data set.
     *
     * @return the repository class, Repository if not set
     */
    Class<? extends Repository> repository() default Repository.class;

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
    int pageSize() default 25;
}
