package net.microfalx.bootstrap.registry;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.core.utils.Json;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

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
        String valueAsString = (String) attributes.get(DATA_VALUE_ATTR);
        if (isNotEmpty(valueAsString)) {
            String dataTypeClassName = (String) attributes.get(DATA_TYPE_ATTR);
            try {
                Class<?> dataTypeClass = getClass().getClassLoader().loadClass(dataTypeClassName);
                return Json.asObject(valueAsString, (Class<T>) dataTypeClass);
            } catch (ClassNotFoundException e) {
                throw new RegistryException("Cannot load class " + dataTypeClassName, e);
            } catch (Exception e) {
                throw new RegistryException("Cannot deserialize class " + dataTypeClassName, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public <T> void set(T value) {
        if (value != null) {
            attributes.put(DATA_VALUE_ATTR, Json.asString(value));
            attributes.put(DATA_TYPE_ATTR, value.getClass().getName());
        } else {
            attributes.remove(DATA_VALUE_ATTR);
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
