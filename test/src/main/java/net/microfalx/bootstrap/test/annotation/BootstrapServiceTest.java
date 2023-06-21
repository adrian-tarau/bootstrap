package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.core.async.AsyncConfig;
import net.microfalx.bootstrap.core.config.I18nConfig;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexProperties;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchProperties;
import net.microfalx.bootstrap.search.SearchService;
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
@ContextConfiguration(classes = {ResourceService.class, ResourceProperties.class,
        IndexService.class, IndexProperties.class, SearchService.class, SearchProperties.class})
@Import({I18nConfig.class, AsyncConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
public @interface BootstrapServiceTest {
}
