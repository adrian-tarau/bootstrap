package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide information about a form.
 * <p>
 * The template specific properties are passed directly to the template engine, and they are specific to
 * each template engine. However, the Data Set uses conventions present in <i>Thymeleaf</i> rendering engine
 * since it is the default template engine.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Form {

    /**
     * Returns the model for the data set model.
     *
     * @return the model class, Object is not set
     */
    Class<?> model() default Object.class;
}
