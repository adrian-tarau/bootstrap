package net.microfalx.bootstrap.model;

import net.microfalx.lang.CollectionUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static net.microfalx.bootstrap.model.AttributeUtils.decodeAttributes;
import static net.microfalx.bootstrap.model.AttributeUtils.sortAndFilter;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ObjectUtils.defaultIfNull;

/**
 * Base class for {@link Attributes}.
 */
public abstract class AbstractAttributes<A extends Attribute> implements Attributes<A> {

    private Map<String, A> attributes;
    private boolean readOnly;

    @Override
    public final A addAttribute(A attribute) {
        requireNonNull(attribute);
        checkReadOnly();
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = this;
        getOrCreateMap().put(attribute.getName().toLowerCase(), attribute);
        return attribute;
    }

    @Override
    public A addAttribute(String name, Object value) {
        requireNonNull(name);
        return addAttribute(createAttribute(name, value));
    }

    @Override
    public final A addAttributeIfAbsent(A attribute) {
        requireNonNull(attribute);
        checkReadOnly();
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = this;
        A prevAttribute = getOrCreateMap().putIfAbsent(attribute.getName().toLowerCase(), attribute);
        return defaultIfNull(prevAttribute, attribute);
    }

    @Override
    public A addAttributeIfAbsent(String name, Object value) {
        requireNonNull(name);
        return addAttributeIfAbsent(createAttribute(name, value));
    }

    @Override
    public final A removeAttribute(String name) {
        requireNonNull(name);
        checkReadOnly();
        if (attributes == null) return null;
        A attribute = attributes.remove(name.toLowerCase());
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = null;
        return attribute;
    }

    @Override
    public final A getAttribute(String name) {
        return getAttribute(name, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final A getAttribute(String name, Object defaultValue) {
        requireNonNull(name);
        A attribute = attributes != null ? attributes.get(name.toLowerCase()) : null;
        if (attribute == null) attribute = (A) Attribute.create(name, defaultValue);
        return attribute;
    }

    @Override
    public final boolean hasAttributes() {
        return !(attributes == null || attributes.isEmpty());
    }

    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }

    protected final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public final void copyFrom(Map<String, Object> values) {
        if (values == null) return;
        values.forEach(this::addAttribute);
    }

    @Override
    public final <AA extends Attribute> void copyFrom(Attributes<AA> attributes) {
        if (attributes == null) return;
        getRawAttributes(attributes).forEach(a -> addAttribute(a.getName(), a.getValue()));
    }

    @Override
    public void copyFrom(Resource resource) throws IOException {
        requireNonNull(resource);
        decodeAttributes(resource, this);
    }

    @Override
    public final Map<String, A> toMap() {
        return attributes != null ? unmodifiableMap(attributes) : emptyMap();
    }

    @SuppressWarnings("unchecked")
    protected abstract A createAttribute(String name, Object value);

    @Override
    public Collection<A> toCollection() {
        return sortAndFilter(attributes.values(), Integer.MAX_VALUE, null);
    }

    @Override
    public Collection<A> toCollection(int maximumCount) {
        return toCollection(maximumCount, null);
    }

    @Override
    public Collection<A> toCollection(int maximumCount, Function<A, Boolean> filter) {
        return sortAndFilter(attributes.values(), maximumCount, filter);
    }

    @Override
    public final Resource toJson() {
        return AttributeUtils.encodeAttributes(this);
    }

    @Override
    public final Iterator<A> iterator() {
        return getRawAttributes(this).iterator();
    }

    private Map<String, A> getOrCreateMap() {
        if (attributes == null) attributes = new HashMap<>();
        return attributes;
    }

    private static <A extends Attribute> Collection<A> getRawAttributes(Attributes<A> attributes) {
        if (attributes instanceof AbstractAttributes<A>) {
            return CollectionUtils.asCollection(((AbstractAttributes<A>) attributes).attributes);
        } else {
            return attributes.toMap().values();
        }
    }

    private void checkReadOnly() {
        if (readOnly) throw new UnsupportedOperationException("Attributes are read only");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributes=" + attributes.size() +
                ", readOnly=" + readOnly +
                '}';
    }
}
