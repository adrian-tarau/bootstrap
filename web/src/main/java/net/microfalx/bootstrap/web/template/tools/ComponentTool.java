package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.component.Actionable;
import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.Container;
import net.microfalx.bootstrap.web.template.TemplateSecurityContext;
import org.thymeleaf.context.IContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.defaultIfNull;

/**
 * Template utilities around components.
 */
public class ComponentTool extends AbstractTool {

    public ComponentTool(IContext context) {
        super(context);
    }

    /**
     * Returns a list of children which should be displayed to the current user.
     *
     * @param component the component
     * @return a non-null instance
     */
    public Collection<Component<?>> getChildren(net.microfalx.bootstrap.web.component.Component<?> component) {
        TemplateSecurityContext securityContext = TemplateSecurityContext.get();
        if (!(component instanceof Container)) return Collections.emptyList();
        Collection<net.microfalx.bootstrap.web.component.Component<?>> children = new ArrayList<>();
        for (net.microfalx.bootstrap.web.component.Component<?> child : ((Container<?>) component).getChildren()) {
            if (child instanceof Actionable) {
                Actionable<?> actionable = (Actionable<?>) child;
                if (actionable.getRoles().isEmpty()) {
                    children.add(child);
                } else if (securityContext.hasRoles(actionable.getRoles())) {
                    children.add(child);
                }
            } else {
                children.add(child);
            }
        }
        return children;
    }

    /**
     * Returns whether the component is a container.
     *
     * @param component the component to validate
     * @return {@code true} if container, {@code false} otherwise
     */
    public boolean isContainer(net.microfalx.bootstrap.web.component.Component<?> component) {
        return component instanceof Container;
    }

    /**
     * Returns the CSS  classes configured for the component.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public String getCssClass(net.microfalx.bootstrap.web.component.Component<?> component) {
        return getCssClass(component, null);
    }

    /**
     * Returns the final list of CSS classes by appending the component CSS classes to the an initial set of classes
     *
     * @param component      the component which provides the classes
     * @param initialClasses the initial classes to be used with the component
     * @return a new list of classes
     */
    public String getCssClass(net.microfalx.bootstrap.web.component.Component<?> component, String initialClasses) {
        if (component == null) return initialClasses;
        initialClasses = defaultIfNull(initialClasses, EMPTY_STRING);
        initialClasses += " " + defaultIfNull(component.getCssClass(), EMPTY_STRING);
        return initialClasses.trim();
    }
}
