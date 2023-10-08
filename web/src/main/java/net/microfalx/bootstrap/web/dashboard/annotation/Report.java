package net.microfalx.bootstrap.web.dashboard.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide reports withing a dashboard.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Report {
    
    /**
     * Returns the position of the report within its parent.
     *
     * @return a positive integer if a position is set, -1 for auto
     */
    int position() default -1;
}
