package net.microfalx.bootstrap.web.component;

import java.util.Set;

/**
 * Base class for all containers with an action.
 *
 * @param <C> the component type
 */
public abstract class ActionableContainer<C extends ActionableContainer<C>> extends Container<C> implements Actionable<C> {

    private final ActionableSupport actionable = new ActionableSupport(this);

    @Override
    public String getAction() {
        return actionable.getAction();
    }

    @Override
    public C setAction(String action) {
        actionable.setAction(action);
        return self();
    }

    @Override
    public String getToken() {
        return actionable.getToken();
    }

    @Override
    public C setToken(String token) {
        actionable.setToken(token);
        return self();
    }

    @Override
    public String getHandler() {
        return actionable.getHandler();
    }

    @Override
    public C setHandler(String handler) {
        actionable.setToken(handler);
        return self();
    }

    @Override
    public Set<String> getRoles() {
        return actionable.getRoles();
    }

    @Override
    public C setRoles(String... roles) {
        return self();
    }

    @Override
    public C addRoles(String... roles) {
        actionable.addRoles(roles);
        return self();
    }

    @Override
    public boolean hasText() {
        return actionable.hasText();
    }

    @Override
    public String getText() {
        return actionable.getText();
    }

    @Override
    public C setText(String text) {
        actionable.setText(text);
        return self();
    }

    @Override
    public String getDescription() {
        return actionable.getDescription();
    }

    @Override
    public C setDescription(String description) {
        actionable.setDescription(description);
        return self();
    }

    @Override
    public String getIcon() {
        return actionable.getIcon();
    }

    @Override
    public C setIcon(String icon) {
        actionable.setIcon(icon);
        return self();
    }

    @Override
    public Style getStyle() {
        return actionable.getStyle();
    }

    @Override
    public C setStyle(Style style) {
        actionable.setStyle(style);
        return self();
    }
}
