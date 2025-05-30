package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.core.async.AsynchronousConfig;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.CustomValidatorBean;

import java.lang.annotation.*;

/**
 * An annotation which enables various internal services.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {I18nService.class, ResourceService.class, ResourceProperties.class, CustomValidatorBean.class, MetadataService.class})
@Import({I18nProperties.class, AsynchronousConfig.class})
//@TestExecutionListeners(MockitoAnswersExecutionListener.class)
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
public @interface BootstrapServiceTest {
}
