package net.microfalx.bootstrap.registry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.microfalx.bootstrap.registry.RegistryUtils.normalizePath;
import static net.microfalx.lang.UriUtils.SLASH;

@Order
@Component
public class MemoryStorage extends AbstractStorage {

    private final Map<String, NodeImpl> nodes = new ConcurrentHashMap<>();

    @Override
    public Collection<Node> getChildren(String path, boolean recursive) {
        String normalizedPath = normalizePath(path);
        return nodes.keySet().stream()
                .filter(nodePath -> isChild(normalizedPath, nodePath, recursive))
                .map(nodes::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Node> getNode(String path) {
        String normalizedPath = normalizePath(path);
        return Optional.ofNullable(nodes.get(normalizedPath));
    }

    @Override
    public boolean exists(String path) {
        String normalizedPath = normalizePath(path);
        NodeImpl node = nodes.get(normalizedPath);
        return node != null && node.isExists();
    }

    @Override
    public byte[] get(String path) {
        String normalizedPath = normalizePath(path);
        return nodes.getOrDefault(normalizedPath, null).data;
    }

    @Override
    public void put(String path, byte[] data) {
        put(path, data, 0);
    }

    @Override
    public void put(String path, byte[] data, int version) {
        String normalizedPath = normalizePath(path);
        LocalDateTime now = LocalDateTime.now();
        NodeImpl node = nodes.computeIfAbsent(normalizedPath, key -> {
            NodeImpl newNode = new NodeImpl(null, key);
            newNode.setCreatedAt(now);
            newNode.setVersion(1);
            return newNode;
        });
        node.setExists(true);
        node.setUpdatedAt(now);
        node.setUpdateCount(node.getUpdateCount() + 1);
        if (version > 0) {
            node.setVersion(version);
        } else {
            node.setVersion(node.getVersion() + 1);
        }
        node.setLeaf(true);
        node.data = data;
    }

    @Override
    public void remove(String path) {
        String normalizedPath = normalizePath(path);
        nodes.remove(normalizedPath);
    }

    private boolean isChild(String parentPath, String childPath, boolean recursive) {
        if (childPath.equals(parentPath)) return false;
        if (!childPath.startsWith(parentPath)) return false;
        String relativePath = childPath.substring(parentPath.length());
        if (relativePath.startsWith(SLASH)) relativePath = relativePath.substring(1);
        if (recursive) {
            return !relativePath.isEmpty();
        } else {
            return !relativePath.isEmpty() && !relativePath.contains(SLASH);
        }
    }
}
