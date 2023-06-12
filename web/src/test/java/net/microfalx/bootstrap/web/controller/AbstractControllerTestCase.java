package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import net.microfalx.bootstrap.web.template.TemplateService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;

@WithUserDetails
@WithMockUser
@ContextConfiguration(classes = {WebContainerService.class, ApplicationService.class, ApplicationProperties.class, TemplateService.class})
public abstract class AbstractControllerTestCase {
}
