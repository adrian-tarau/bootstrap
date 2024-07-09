package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

/**
 * A formatting tool which used the data set formatters.
 */
public class FormatTool extends AbstractTool {

    public FormatTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    /**
     * Formats an object.
     */
    public String format(Object value) {
        return FormatterUtils.basicFormatting(value, null);
    }
}
