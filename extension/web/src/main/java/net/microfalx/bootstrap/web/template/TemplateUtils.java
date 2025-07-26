package net.microfalx.bootstrap.web.template;

import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;

/**
 * Various utilities around template.
 */
public class TemplateUtils {

    /**
     * Returns an attribute of the model.
     *
     * @param context the template context
     * @param name    the attribute name
     * @return the value, null if does not exist
     */
    @SuppressWarnings("unchecked")
    public static <T> T getModelAttribute(IContext context, String name) {
        if (context instanceof IWebContext) {
            return (T) ((IWebContext) context).getExchange().getAttributeValue(name);
        } else {
            return null;
        }
    }

    /**
     * Returns whether an attribute exists in  the model.
     *
     * @param context the template context
     * @param name    the attribute name
     * @return {@code true} if exists, {@code false} otherwise
     */
    public static boolean containsModelAttribute(IContext context, String name) {
        if (context instanceof IWebContext) {
            return ((IWebContext) context).getExchange().containsAttribute(name);
        } else {
            return false;
        }
    }

    /**
     * Changes an attribute in the model
     *
     * @param context the template context
     * @param name    the attribute name
     * @param value   the attribute value
     * @param <T>     the type of the attribute value
     */
    public static <T> void setModelAttribute(IContext context, String name, T value) {
        if (context instanceof IWebContext) {
            ((IWebContext) context).getExchange().setAttributeValue(name, value);
        }
    }
}
