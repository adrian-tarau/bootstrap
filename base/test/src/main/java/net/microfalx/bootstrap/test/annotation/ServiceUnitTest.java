package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.test.extension.BootstrapExtension;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.*;

/**
 * An annotation used with unit tests which enables various internal services (or components) to test
 * a service (or a component).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith({MockitoExtension.class, InstancioExtension.class, BootstrapExtension.class})
public @interface ServiceUnitTest {
}
