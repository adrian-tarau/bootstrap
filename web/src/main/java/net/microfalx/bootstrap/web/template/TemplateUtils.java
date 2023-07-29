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
}
