package net.microfalx.bootstrap.registry;

import net.microfalx.bootstrap.core.utils.Json;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.microfalx.bootstrap.registry.RegistryUtils.normalizePath;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

final class RegistryImpl implements Registry {

    private final RegistryService registryService;

    RegistryImpl(RegistryService registryService) {
        requireNonNull(registryService);
        this.registryService = registryService;
    }

    @Override
    public boolean walk(String path, int depth, BiFunction<String, Node, Boolean> visitor) {
        requireNotEmpty(path);
        requireNonNull(visitor);
        Storage storage = getStorage();
        return walkInternal(path, depth, visitor, storage);
    }

    @Override
    public Iterable<Data> list(String path) {
        requireNotEmpty(path);
        return list(path, node -> true);
    }

    @Override
    public Iterable<Data> list(String path, Function<Node, Boolean> filter) {
        requireNotEmpty(path);
        requireNonNull(filter);
        path = normalizePath(path);
        Storage storage = getStorage();
        Collection<Node> children = storage.getChildren(path, false);
        return children.stream()
                .filter(filter::apply)
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String path) {
        requireNotEmpty(path);
        path = normalizePath(path);
        Storage storage = getStorage();
        return storage.exists(path);
    }

    @Override
    public Optional<Node> lookup(String path) {
        requireNotEmpty(path);
        path = normalizePath(path);
        Storage storage = getStorage();
        return storage.getNode(path);
    }

    @Override
    public Optional<Data> get(String path) {
        requireNotEmpty(path);
        path = normalizePath(path);
        Optional<Node> node = lookup(path);
        return node.map(this::toData);
    }

    @Override
    public Data getOrCreate(String path) {
        requireNotEmpty(path);
        path = normalizePath(path);
        Optional<Data> data = get(path);
        return data.orElse(new DataImpl(path));
    }

    @Override
    public void set(Data data) {
        requireNonNull(data);
        String path = normalizePath(data.getNode().getPath());
        Storage storage = getStorage();
        byte[] json = Json.asBytes(((DataImpl) data).attributes);
        storage.put(path, json);
    }

    private boolean walkInternal(String path, int depth, BiFunction<String, Node, Boolean> visitor, Storage storage) {
        Optional<Node> node = storage.getNode(path);
        if (node.isEmpty()) return true;
        Boolean continueWalking = visitor.apply(path, node.get());
        if (!Boolean.TRUE.equals(continueWalking)) return false;
        if (depth <= 0) return true;
        Collection<Node> children = storage.getChildren(path, false);
        for (Node child : children) {
            if (!walkInternal(child.getPath(), depth - 1, visitor, storage)) return false;
        }
        return true;
    }

    private Data toData(Node node) {
        NodeImpl nodeImpl = new NodeImpl(node);
        Storage storage = getStorage();
        byte[] dataBytes = storage.get(node.getPath());
        try {
            Map<String, Object> attributes = Json.asMap(dataBytes);
            return new DataImpl(nodeImpl, attributes);
        } catch (IOException e) {
            throw new RegistryException("Failed to deserialize data for node '" + node.getPath() + "'", e);
        }
    }

    Storage getStorage() {
        return registryService.getStorage();
    }

}
