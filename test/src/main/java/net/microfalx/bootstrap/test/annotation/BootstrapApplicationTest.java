package net.microfalx.bootstrap.test.annotation;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.CustomValidatorBean;

import java.lang.annotation.*;

/**
 * An annotation which enables services used to build a web application.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ContextConfiguration(classes = {I18nService.class, ResourceService.class, ResourceProperties.class, CustomValidatorBean.class, MetadataService.class,
        WebContainerService.class, ApplicationService.class, ApplicationProperties.class,
        IndexService.class, SearchService.class, DataSetService.class})
@BootstrapServiceTest
public @interface BootstrapApplicationTest {
}
