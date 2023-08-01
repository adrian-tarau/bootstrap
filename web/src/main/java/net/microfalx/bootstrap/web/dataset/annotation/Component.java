package net.microfalx.bootstrap.web.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide UI widgit configuraion for data sets fields.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * Returns the type of the component.
     *
     * @return a non-null instance
     */
    Type value() default Type.TEXT_FIELD;

    /**
     * An enum for the component type.
     */
    enum Type {
        TEXT_FIELD,
        TEXT_AREA,
        PASSWORD
    }
}
