package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import org.springframework.test.context.ContextConfiguration;

/**
 * An annotation which enables services used to build a web application.
 */
@ContextConfiguration(classes = {WebContainerService.class, ApplicationService.class, ApplicationProperties.class})
public @interface BootstrapApplicationTest {
}
