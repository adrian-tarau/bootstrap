package net.microfalx.bootstrap.web.component;

import net.microfalx.bootstrap.model.Parameters;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.immutableSet;

/**
 * Helper code for all actionables.
 */
public class ActionableSupport implements Actionable<ActionableSupport> {

    private final Component<?> owner;
    private String token;
    private String action;
    private String target;
    private Set<String> roles;
    private ItemableSupport itemable;
    private final Parameters parameters = Parameters.create();

    public ActionableSupport(Component<?> owner) {
        requireNonNull(owner);
        this.owner = owner;
        this.itemable = new ItemableSupport(owner);
    }

    public Component<?> getOwner() {
        return owner;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public ActionableSupport setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public ActionableSupport setTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public ActionableSupport setToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public Set<String> getRoles() {
        return immutableSet(roles);
    }

    @Override
    public ActionableSupport setRoles(String... roles) {
        if (this.roles != null) this.roles.clear();
        if (this.roles == null) this.roles = new HashSet<>();
        this.roles.addAll(asList(roles));
        return this;
    }

    @Override
    public ActionableSupport addRoles(String... roles) {
        if (this.roles == null) this.roles = new HashSet<>();
        this.roles.addAll(asList(roles));
        return this;
    }

    public boolean hasText() {
        return itemable.hasText();
    }

    public String getText() {
        return itemable.getText();
    }

    public ActionableSupport setText(String text) {
        this.itemable.setText(text);
        return this;
    }

    @Override
    public String getDescription() {
        return itemable.getDescription();
    }

    @Override
    public ActionableSupport setDescription(String description) {
        itemable.setDescription(description);
        return this;
    }

    public String getIcon() {
        return itemable.getIcon();
    }

    public ActionableSupport setIcon(String icon) {
        this.itemable.setIcon(icon);
        return this;
    }

    public Style getStyle() {
        return itemable.getStyle();
    }

    public ActionableSupport setStyle(Style style) {
        this.itemable.setStyle(style);
        return this;
    }

    @Override
    public ActionableSupport addParameter(String name, Object value) {
        parameters.add(name, value);
        return this;
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "ActionableSupport{" +
                "owner=" + owner +
                ", token='" + token + '\'' +
                ", action='" + action + '\'' +
                ", roles=" + roles +
                ", parameters=" + parameters +
                ", itemable=" + itemable +
                '}';
    }
}
