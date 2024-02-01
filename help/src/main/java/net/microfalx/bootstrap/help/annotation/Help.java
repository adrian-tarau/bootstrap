package net.microfalx.bootstrap.help.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Help {

    /**
     * Returns the path to the help resource.
     *
     * @return a non-null instance
     */
    String value();
}
