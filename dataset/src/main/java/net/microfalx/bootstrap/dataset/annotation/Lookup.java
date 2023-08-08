package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide a model class to fields to be used to calculate the display name
 * instead of the default display name.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lookup {

    /**
     * Returns the model for the data set model.
     *
     * @return the model class, Object is not set
     */
    Class<?> model() default Object.class;
}
