package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Holds a link to an application resource.
 */
public class Link implements Identifiable, Nameable {

    private String id;
    private final String name;
    private final Set<String> roles = new HashSet<>();
    private String target;
    private final List<Link> links = new ArrayList<>();
    private Link parent;
    private String icon;
    private int order = -1;

    public Link(String name) {
        requireNotEmpty(name);
        this.name = name;
        updateId();
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Link getParent() {
        return parent;
    }

    public Set<String> getRoles() {
        return unmodifiableSet(roles);
    }

    public void addRole(String role) {
        requireNotEmpty(role);
        this.roles.add(role);
    }

    public String getTarget() {
        return target;
    }

    public Link setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public Link setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public Link setOrder(int order) {
        this.order = Math.max(1, order);
        return this;
    }

    public Collection<Link> getLinks() {
        return unmodifiableCollection(links);
    }

    public void add(Link link) {
        requireNonNull(link);
        if (link.order == -1) link.order = this.links.size() * 10;
        this.links.add(link);
        this.links.sort(Comparator.comparingInt(Link::getOrder));
        link.parent = this;
    }

    private void updateId() {
        this.id = toIdentifier(name);
        Link parent = this.parent;
        while (parent != null) {
            this.id = parent.getId() + "_" + this.id;
            parent = parent.parent;
        }
    }

    @Override
    public String toString() {
        return "Link{" +
                "name='" + name + '\'' +
                ", roles=" + roles +
                ", target='" + target + '\'' +
                ", links=" + links +
                '}';
    }
}
