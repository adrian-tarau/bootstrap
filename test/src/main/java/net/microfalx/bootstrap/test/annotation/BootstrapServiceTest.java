package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.core.async.AsyncConfig;
import net.microfalx.bootstrap.core.i18n.I18nConfig;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

/**
 * An annotation which enables various internal services.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {ResourceService.class, ResourceProperties.class})
@Import({I18nConfig.class, AsyncConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
public @interface BootstrapServiceTest {
}
