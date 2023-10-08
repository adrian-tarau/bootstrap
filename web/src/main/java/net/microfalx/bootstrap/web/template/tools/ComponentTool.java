package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.component.Actionable;
import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.Container;
import net.microfalx.bootstrap.web.component.panel.BasePanel;
import net.microfalx.bootstrap.web.component.renderer.ComponentRenderer;
import net.microfalx.bootstrap.web.component.renderer.EmptyComponentRenderer;
import net.microfalx.bootstrap.web.template.TemplateSecurityContext;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
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

    private ComponentRenderer renderer;
    private ApplicationService applicationService;

    public ComponentTool(IContext context, ApplicationService applicationService) {
        super(context);
        this.applicationService = applicationService;
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
            if (child instanceof Actionable<?> actionable) {
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
     * Returns whether the component supports a title and it the title is set (non-empty).
     *
     * @param component the component
     * @return <code>true</code> if it has a title, <code>false</code> otherwise
     */
    public boolean hasTitle(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (component instanceof BasePanel<?> panel) {
            return StringUtils.isNotEmpty(panel.getTitle());
        } else {
            return false;
        }
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
        initialClasses += " " + getRenderer().getCssClass(component);
        return initialClasses.trim();
    }

    /**
     * Returns the template which renders a given component type.
     *
     * @param component the component
     * @return the fragment
     */
    public String getComponentFragment(net.microfalx.bootstrap.web.component.Component<?> component) {
        return "render-" + component.getType();
    }

    /**
     * Returns the component renderer associated with the current theme.
     *
     * @return a non-null instance
     */
    private ComponentRenderer getRenderer() {
        if (renderer == null) {
            Collection<ComponentRenderer> componentRenderers = ClassUtils.resolveProviderInstances(ComponentRenderer.class);
            for (ComponentRenderer componentRenderer : componentRenderers) {
                if (componentRenderer.getId().equalsIgnoreCase(applicationService.getApplication().getTheme().getId())) {
                    renderer = componentRenderer;
                    break;
                }
            }
            if (renderer == null) renderer = new EmptyComponentRenderer();
        }
        return renderer;
    }
}
