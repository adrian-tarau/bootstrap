package net.microfalx.bootstrap.test.annotation;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.lang.annotation.*;

/**
 * An annotation which disables the security filters.
 * <p>
 * This annotation can be used with tests to turn off security (even if it is enabled).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigureMockMvc(addFilters = false)
public @interface DisableSecurity {
}
