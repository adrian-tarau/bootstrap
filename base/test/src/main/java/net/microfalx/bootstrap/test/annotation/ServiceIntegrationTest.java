package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.configuration.ConfigurationService;
import net.microfalx.bootstrap.core.async.AsynchronousConfiguration;
import net.microfalx.bootstrap.core.config.ConverterConfiguration;
import net.microfalx.bootstrap.core.config.RetryConfiguration;
import net.microfalx.bootstrap.core.config.RetryProperties;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.registry.RegistryConfiguration;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.store.StoreProperties;
import net.microfalx.bootstrap.store.StoreService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * An annotation used with integration tests which enables various internal services to test a service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
        // properties
        RetryProperties.class, I18nProperties.class, StoreProperties.class, ResourceProperties.class,
        // configurations
        RetryConfiguration.class, AsynchronousConfiguration.class, ConverterConfiguration.class,
        //services
        I18nService.class, StoreService.class, ResourceService.class, RegistryConfiguration.class,
        RegistryService.class, ConfigurationService.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
public @interface ServiceIntegrationTest {
}
