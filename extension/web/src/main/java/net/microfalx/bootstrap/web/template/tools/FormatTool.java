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

    /**
     * Formats a numeric object as bytes.
     */
    public String formatBytes(Object value) {
        return net.microfalx.lang.FormatterUtils.formatBytes(value);
    }

    /**
     * Formats temporal as a date/time.
     */
    public String formatDateTime(Object value) {
        return net.microfalx.lang.FormatterUtils.formatDateTime(value);
    }

    /**
     * Formats the date component of temporal.
     */
    public String formatDate(Object value) {
        return net.microfalx.lang.FormatterUtils.formatDate(value);
    }

    /**
     * Formats the time component of temporal.
     */
    public String formatTime(Object value) {
        return net.microfalx.lang.FormatterUtils.formatTime(value);
    }

    /**
     * Formats a duration.
     */
    public String formatDuration(Object value) {
        return net.microfalx.lang.FormatterUtils.formatDuration(value);
    }

    /**
     * Formats a duration as elapsed (XXX ago).
     */
    public String formatElapsed(Object value) {
        return net.microfalx.lang.FormatterUtils.formatElapsed(value);
    }
}
