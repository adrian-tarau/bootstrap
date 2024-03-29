package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import net.microfalx.bootstrap.web.template.TemplateService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;

@WithUserDetails
@WithMockUser
@ContextConfiguration(classes = {WebContainerService.class, ApplicationService.class, ApplicationProperties.class,
        TemplateService.class, MetadataService.class, DataSetService.class,
        ResourceService.class, ResourceProperties.class})
public abstract class AbstractControllerTestCase {
}
