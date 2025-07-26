package net.microfalx.bootstrap.web.dashboard.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dashboard {

    /**
     * Returns the dashboard identifier.
     *
     * @return a non-null instance
     */
    String value();
}
