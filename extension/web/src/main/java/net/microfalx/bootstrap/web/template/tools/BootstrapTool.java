package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Theme;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

public class BootstrapTool extends AbstractTool {

    public BootstrapTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    /**
     * Returns the CSS classes to show the object as being active.
     *
     * @param value1 the first value to compare
     * @param value2 the second value to compare
     * @return the classes, null if there is no alert
     */
    public String getActiveClass(Object value1, Object value2) {
        String css = StringUtils.EMPTY_STRING;
        if (ObjectUtils.equals(value1, value2)) {
            css = "active";
        }
        return css;
    }

    /**
     * Returns whether the theme asks for dark mode.
     *
     * @return {@code true} if dark mode, {@code false} otherwise
     */
    public boolean isDarkTheme() {
        return getTheme().getMode() == Theme.Mode.DARK;
    }

    /**
     * Returns whether the theme asks for light mode.
     *
     * @return {@code true} if light mode, {@code false} otherwise
     */
    public boolean isLightTheme() {
        return getTheme().getMode() == Theme.Mode.LIGHT;
    }

    private Theme getTheme() {
        ApplicationService applicationService = applicationContext.getBean(ApplicationService.class);
        return applicationService.getCurrentTheme();
    }
}
