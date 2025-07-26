package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.model.Parameters;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.component.*;
import net.microfalx.bootstrap.web.component.panel.BasePanel;
import net.microfalx.bootstrap.web.component.renderer.ComponentRenderer;
import net.microfalx.bootstrap.web.component.renderer.EmptyComponentRenderer;
import net.microfalx.bootstrap.web.template.TemplateSecurityContext;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static net.microfalx.bootstrap.web.component.renderer.ComponentRenderer.CSS_STYLE_SEPARATOR;
import static net.microfalx.lang.StringUtils.*;

/**
 * Template utilities around components.
 */
@SuppressWarnings("unused")
public class ComponentTool extends AbstractTool {

    private ComponentRenderer renderer;
    private final ApplicationService applicationService;
    private final LinkTool linkTool;
    private final String action;

    public ComponentTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
        this.linkTool = new LinkTool(templateContext, applicationContext);
        this.action = StringUtils.removeEndSlash(linkTool.getSelf());
        this.applicationService = applicationContext.getBean(ApplicationService.class);
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
     * Returns whether the component indicates based on the URI path that it should be active.
     * <p>
     * Mostly it applies to {@link Actionable}.
     *
     * @param component the component
     * @return {@code true} if the component should be active, {@code false} otherwise
     */
    public boolean isActive(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (component instanceof Actionable<?> actionable) {
            String action = actionable.getAction();
            boolean active = ObjectUtils.equals(action, this.action);
            if (active) return true;
            if (component instanceof Container<?> container) {
                for (net.microfalx.bootstrap.web.component.Component<?> child : ((Container<?>) component).getChildren()) {
                    if (isActive(child)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the component is a separator (menu, toolbar, etc).
     *
     * @param component the component
     * @return {@code true} if separator, @{code false} otherwise
     */
    public boolean isSeparator(net.microfalx.bootstrap.web.component.Component<?> component) {
        return component instanceof Separator;
    }

    /**
     * Returns the class or classes which shows an active component.
     *
     * @param component the component
     * @return the class or classes if the component is active, null otherwise
     * @see #isActive(Component)
     */
    public String getActiveClass(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (!isActive(component)) return null;
        if (component instanceof Actionable<?> actionable) {
            return component instanceof Container<?> container ? "d-block" : "active";
        } else {
            return null;
        }
    }

    /**
     * Returns the class or classes which shows an active component.
     *
     * @param component the component
     * @return the class or classes if the component is active, null otherwise
     * @see #isActive(Component)
     */
    public String getActiveStyle(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (!isActive(component)) return null;
        if (component instanceof Actionable<?> && component instanceof Container<?>) {
            return "display:block";
        } else {
            return null;
        }
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
     * Returns the CSS  classes configured for the component.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public String getCssStyle(net.microfalx.bootstrap.web.component.Component<?> component) {
        return getCssStyle(component, null);
    }

    /**
     * Returns whether the component supports a title and it the title is set (non-empty).
     *
     * @param component the component
     * @return <code>true</code> if it has a title, <code>false</code> otherwise
     */
    public boolean hasTitle(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (component instanceof BasePanel<?> panel) {
            return isNotEmpty(panel.getTitle());
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
     * Returns the final list of CSS styles by appending the component CSS styles to the an initial set of styles
     *
     * @param component      the component which provides the classes
     * @param initialClasses the initial classes to be used with the component
     * @return a new list of classes
     */
    public String getCssStyle(net.microfalx.bootstrap.web.component.Component<?> component, String initialClasses) {
        if (component == null) return initialClasses;
        StringBuilder builder = new StringBuilder();
        append(builder, nullIfEmpty(initialClasses), CSS_STYLE_SEPARATOR);
        append(builder, nullIfEmpty(component.getCssStyles()), CSS_STYLE_SEPARATOR);
        append(builder, nullIfEmpty(getRenderer().getCssStyle(component)), CSS_STYLE_SEPARATOR);
        if (isNotEmpty(component.getMaxWidth())) {
            append(builder, "max-width: " + component.getMaxWidth(), CSS_STYLE_SEPARATOR);
            append(builder, "overflow-x: auto", CSS_STYLE_SEPARATOR);
        }
        if (isNotEmpty(component.getMaxHeight())) {
            append(builder, "max-height: " + component.getMaxHeight(), CSS_STYLE_SEPARATOR);
            append(builder, "overflow-y: auto", CSS_STYLE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * Returns whether the component has an icon associated with it.
     *
     * @param component the component
     * @return {@code true} if there is an icon, {@code false} otherwise
     */
    public boolean hasIcon(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (!(component instanceof Itemable<?> itemable)) return false;
        return itemable.getStyle() != Itemable.Style.TEXT && isNotEmpty(itemable.getIcon());
    }

    /**
     * Returns the CSS classes to represent to icon for the component.
     *
     * @param component the component
     * @return the CSS classes, null if it has no icon
     */
    public String getIconClass(net.microfalx.bootstrap.web.component.Component<?> component) {
        return hasIcon(component) ? ((Itemable<?>) component).getIcon() : null;
    }

    /**
     * Returns arguments for a JavaScript function call from a component.
     * <p>
     * Only {@link Actionable} carries parameters which can be converted to arguments.
     *
     * @param component the component
     * @return the arguments, empty string if parameters are empty or not available.
     */
    public String getFunctionArguments(net.microfalx.bootstrap.web.component.Component<?> component) {
        if (!(component instanceof Actionable<?> actionable)) return EMPTY_STRING;
        Parameters parameters = actionable.getParameters();
        if (parameters.isEmpty()) return EMPTY_STRING;
        String convertedParams = parameters.toValues().stream()
                .map(this::getJavaScriptValue)
                .collect(Collectors.joining(", "));
        return ", " + convertedParams;
    }

    /**
     * Returns the template which renders a given component type.
     * <p>
     * The method accepts any component but renders an HTML fragment to indicate the value is not a component if it is
     * passed by mistake
     *
     * @param value the value
     * @return the fragment
     */
    public String getComponentFragment(Object value) {
        if (!(value instanceof net.microfalx.bootstrap.web.component.Component<?> component)) {
            return "render-invalid-component";
        } else {
            return "render-" + component.getType();
        }
    }

    /**
     * Converts a Java object to a JavaScript object.
     *
     * @param value the original Java object
     * @return the JavaScript object
     */
    private String getJavaScriptValue(Object value) {
        switch (value) {
            case null -> {
                return "null";
            }
            case String s -> {
                return "'" + StringEscapeUtils.escapeEcmaScript(s) + "'";
            }
            case Number number -> {
                if (value instanceof Double || value instanceof Float) {
                    return Double.toString(((Number) value).doubleValue());
                } else {
                    return Long.toString(((Number) value).longValue());
                }
            }
            default -> {
                return "'" + toJson(value) + "'";
            }
        }
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
