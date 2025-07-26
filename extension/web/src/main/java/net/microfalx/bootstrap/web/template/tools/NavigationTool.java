package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.component.Menu;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

/**
 * Template utilities around navigation.
 */
@SuppressWarnings("unused")
public class NavigationTool extends AbstractTool {

    private final ApplicationService applicationService;

    public NavigationTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
        this.applicationService = applicationContext.getBean(ApplicationService.class);
    }

    /**
     * Returns the navigation with a given identifier.
     *
     * @param id the identifier
     * @return a non-null instance
     */
    public Menu get(String id) {
        return applicationService.getNavigation(id);
    }
}
