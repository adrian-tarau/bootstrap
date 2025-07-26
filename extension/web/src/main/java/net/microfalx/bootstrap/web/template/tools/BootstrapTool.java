package net.microfalx.bootstrap.web.template.tools;

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
}
