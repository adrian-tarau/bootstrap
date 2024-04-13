package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import org.thymeleaf.context.IContext;

/**
 * A formatting tool which used the data set formatters.
 */
public class FormatTool extends AbstractTool {

    public FormatTool(IContext context) {
        super(context);
    }

    /**
     * Formats an object.
     */
    public String format(Object value) {
        return FormatterUtils.basicFormatting(value, null);
    }
}
