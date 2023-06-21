package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.core.async.AsyncConfig;
import net.microfalx.bootstrap.core.config.I18nConfig;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexProperties;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchProperties;
import net.microfalx.bootstrap.search.SearchService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

/**
 * An annotation which enables various internal services.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {ResourceService.class, ResourceProperties.class,
        IndexService.class, IndexProperties.class, SearchService.class, SearchProperties.class})
@Import({I18nConfig.class, AsyncConfig.class})
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ExtendWith(SpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
public @interface BootstrapServiceTest {
}
