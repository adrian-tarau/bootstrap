package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.component.Menu;
import org.thymeleaf.context.IContext;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Template utilities around navigation.
 */
public class NavigationTool extends AbstractTool {

    private final ApplicationService applicationService;

    public NavigationTool(IContext context, ApplicationService applicationService) {
        super(context);
        requireNonNull(applicationService);
        this.applicationService = applicationService;
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
