package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Identifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds the application navigation.
 */
public final class Navigation implements Identifiable<String> {

    private final String id;
    private final List<Link> links = new ArrayList<>();

    public Navigation(String id) {
        requireNonNull(id);
        this.id = id.toLowerCase();
    }

    @Override
    public String getId() {
        return id;
    }

    public Collection<Link> getLinks() {
        return unmodifiableCollection(links);
    }

    public void add(Link link) {
        requireNonNull(link);
        if (link.getOrder() == -1) link.setOrder(this.links.size() * 10);
        this.links.add(link);
        this.links.sort(Comparator.comparingInt(Link::getOrder));
    }

    @Override
    public String toString() {
        return "Navigation{" +
                "id='" + id + '\'' +
                ", links=" + links +
                '}';
    }
}
