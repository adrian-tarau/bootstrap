package net.microfalx.bootstrap.registry;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Getter
@ToString
final class DataImpl implements Data {

    private static final String DATA_VALUE_ATTR = "$DATA_VALUE$";
    private static final String DATA_TYPE_ATTR = "$DATA_CLASS$";

    private final NodeImpl node;
    private final boolean exists;
    final Map<String, Object> attributes = new HashMap<>();

    DataImpl(NodeImpl node, boolean exists) {
        requireNonNull(node);
        this.node = node;
        this.exists = exists;
    }

    DataImpl(NodeImpl node, Map<String, Object> attributes) {
        this.node = node;
        this.exists = true;
        this.attributes.putAll(attributes);
    }

    DataImpl(String path) {
        this.node = new NodeImpl(null, path);
        this.exists = false;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get() {
        return (T) attributes.get(DATA_VALUE_ATTR);
    }

    @Override
    public <T> void set(T value) {
        attributes.put(DATA_VALUE_ATTR, value);
        if (value != null) {
            attributes.put(DATA_TYPE_ATTR, value.getClass().getName());
        } else {
            attributes.remove(DATA_TYPE_ATTR);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        requireNotEmpty(name);
        attributes.put(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String name) {
        requireNotEmpty(name);
        return (T) attributes.get(name);
    }

    @Override
    public String getAttribute(String name, String defaultValue) {
        requireNotEmpty(name);
        Object value = attributes.get(name);
        if (value == null) return defaultValue;
        return value.toString();
    }

    @Override
    public int getAttribute(String name, int defaultValue) {
        requireNotEmpty(name);
        Object value = attributes.get(name);
        return switch (value) {
            case null -> defaultValue;
            case Number number -> number.intValue();
            case String s -> Integer.parseInt(s);
            default -> throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().getName());
        };
    }

    @Override
    public long getAttribute(String name, long defaultValue) {
        requireNotEmpty(name);
        Object value = attributes.get(name);
        return switch (value) {
            case null -> defaultValue;
            case Number number -> number.longValue();
            case String s -> Integer.parseInt(s);
            default -> throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().getName());
        };
    }
}
