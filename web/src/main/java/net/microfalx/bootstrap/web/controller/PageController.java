package net.microfalx.bootstrap.web.controller;

import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.annotation.Name;
import org.apache.commons.lang3.StringUtils;

/**
 * A controller for an application page.
 */
public abstract class PageController {

    public static final String HOME = "index";

    /**
     * Returns the name of the page.
     * <p>
     * It uses {@link Name} annotation or the simple class name (removes Controller and beautifies the camel-case name).
     *
     * @return the name of the page
     */
    protected String getName() {
        Name nameAnnot = AnnotationUtils.getAnnotation(this, Name.class);
        if (nameAnnot != null) {
            return nameAnnot.value();
        } else {
            String simpleName = getClass().getSimpleName();
            simpleName = StringUtils.replaceOnce(simpleName, "Controller", "");
            return net.microfalx.lang.StringUtils.beautifyCamelCase(simpleName);
        }
    }
}
