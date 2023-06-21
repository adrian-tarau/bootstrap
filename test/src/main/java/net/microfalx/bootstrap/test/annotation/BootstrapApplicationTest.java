package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

/**
 * An annotation which enables services used to build a web application.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {WebContainerService.class, ApplicationService.class, ApplicationProperties.class})
@BootstrapServiceTest
public @interface BootstrapApplicationTest {
}
