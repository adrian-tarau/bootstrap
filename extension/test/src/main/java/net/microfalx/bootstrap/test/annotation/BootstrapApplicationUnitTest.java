package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.test.extension.BootstrapTestExtension;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * An annotation used with unit tests which enables services used to build a (web) application.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith({InstancioExtension.class, BootstrapTestExtension.class})
public @interface BootstrapApplicationUnitTest {
}
