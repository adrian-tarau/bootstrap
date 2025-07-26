package net.microfalx.bootstrap.model;

import net.microfalx.lang.CollectionUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static net.microfalx.bootstrap.model.AttributeUtils.decodeJson;
import static net.microfalx.bootstrap.model.AttributeUtils.sortAndFilter;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ObjectUtils.defaultIfNull;

/**
 * Base class for {@link Attributes}.
 * <p>
 * The attribute/parameters preserve the order of insertion.
 */
public abstract class AbstractAttributes<A extends Attribute> implements Attributes<A> {

    private static final Function<Attribute, Boolean> ALL_ATTRIBUTES = attribute -> true;
    private static final Function<String, Boolean> ALL_ATTRIBUTE_NAMES = attribute -> true;

    private Map<String, A> attributes;
    private boolean readOnly;

    @Override
    public final A add(A attribute) {
        requireNonNull(attribute);
        checkReadOnly();
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = this;
        getOrCreateMap().put(attribute.getName().toLowerCase(), attribute);
        return attribute;
    }

    @Override
    public A add(String name, Object value) {
        requireNonNull(name);
        return add(createAttribute(name, value));
    }

    @Override
    public final A addIfAbsent(A attribute) {
        requireNonNull(attribute);
        checkReadOnly();
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = this;
        A prevAttribute = getOrCreateMap().putIfAbsent(attribute.getName().toLowerCase(), attribute);
        return defaultIfNull(prevAttribute, attribute);
    }

    @Override
    public A addIfAbsent(String name, Object value) {
        requireNonNull(name);
        return addIfAbsent(createAttribute(name, value));
    }

    @Override
    public final A remove(String name) {
        requireNonNull(name);
        checkReadOnly();
        if (attributes == null) return null;
        A attribute = attributes.remove(name.toLowerCase());
        if (attribute instanceof AbstractAttribute) ((AbstractAttribute) attribute).parent = null;
        return attribute;
    }

    @Override
    public final A get(String name) {
        return get(name, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final A get(String name, Object defaultValue) {
        requireNonNull(name);
        A attribute = attributes != null ? attributes.get(name.toLowerCase()) : null;
        if (attribute == null) attribute = (A) Attribute.create(name, defaultValue);
        return attribute;
    }

    @Override
    public final boolean isEmpty() {
        return attributes == null || attributes.isEmpty();
    }

    @Override
    public int size() {
        return attributes != null ? attributes.size() : 0;
    }

    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }

    protected final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public final Attributes<A> copyFrom(Map<String, Object> values) {
        if (values == null) return this;
        values.forEach(this::add);
        return this;
    }

    @Override
    public Attributes<A> copyFrom(Map<String, Object> values, Function<String, Boolean> filter) {
        if (values == null) return this;
        Function<String, Boolean> finalFilter = filter == null ? (Function<String, Boolean>) ALL_ATTRIBUTE_NAMES : filter;
        values.entrySet().stream().filter(e -> finalFilter.apply(e.getKey())).forEach(e -> add(e.getKey(), e.getValue()));
        return this;
    }

    @Override
    public final <AA extends Attribute> Attributes<A> copyFrom(Attributes<AA> attributes) {
        if (attributes == null) return this;
        getRawAttributes(attributes).forEach(a -> add(a.getName(), a.getValue()));
        return this;
    }

    @Override
    public <AA extends Attribute> Attributes<A> copyFrom(Attributes<AA> attributes, Function<AA, Boolean> filter) {
        if (attributes == null) return this;
        Function<AA, Boolean> finalFilter = filter == null ? (Function<AA, Boolean>) ALL_ATTRIBUTES : filter;
        getRawAttributes(attributes).stream().filter(a -> finalFilter.apply(a)).forEach(a -> add(a.getName(), a.getValue()));
        return this;
    }

    @Override
    public Attributes<A> copyFrom(Resource resource) throws IOException {
        requireNonNull(resource);
        decodeJson(resource, this);
        return this;
    }

    @Override
    public final Map<String, A> toMap() {
        return attributes != null ? unmodifiableMap(attributes) : emptyMap();
    }

    @Override
    public Set<String> getNames() {
        return attributes != null ? unmodifiableSet(attributes.keySet()) : emptySet();
    }

    @Override
    public Collection<Object> toValues() {
        return attributes != null ? unmodifiableCollection(attributes.values().stream().map(Attribute::getValue).collect(Collectors.toList())) : emptyList();
    }

    @SuppressWarnings("unchecked")
    protected abstract A createAttribute(String name, Object value);

    @Override
    public Collection<A> toCollection() {
        return attributes != null ? sortAndFilter(attributes.values(), Integer.MAX_VALUE, null) : emptyList();
    }

    @Override
    public Collection<A> toCollection(int maximumCount) {
        return toCollection(maximumCount, null);
    }

    @Override
    public Collection<A> toCollection(int maximumCount, Function<A, Boolean> filter) {
        return attributes != null ? sortAndFilter(attributes.values(), maximumCount, filter) : emptyList();
    }

    @Override
    public final Resource toJson() {
        return AttributeUtils.encodeJson(this);
    }

    @Override
    public Resource toProperties() {
        return AttributeUtils.encodeProperties(this);
    }

    @Override
    public String replaceVariables(String text) {
        return AttributeUtils.replaceVariables(this, text);
    }

    @Override
    public final Iterator<A> iterator() {
        return getRawAttributes(this).iterator();
    }

    private Map<String, A> getOrCreateMap() {
        if (attributes == null) attributes = new LinkedHashMap<>();
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
                "attributes=" + (attributes != null ? attributes.size() : 0) +
                ", readOnly=" + readOnly +
                '}';
    }
}
