package net.microfalx.bootstrap.registry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.UriUtils.SLASH;

@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
final class NodeImpl implements Node {

    private final Node parent;
    private final String path;

    private boolean exists;
    private boolean leaf;
    private int updateCount;
    private int version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    byte[] data;

    NodeImpl(Node node) {
        requireNonNull(node);
        this.parent = null;
        this.path = node.getPath();
        this.exists = node.exists();
        this.leaf = node.isLeaf();
        this.updateCount = node.getUpdateCount();
        this.version = node.getVersion();
        this.createdAt = node.getCreatedAt();
        this.updatedAt = node.getUpdatedAt();
    }

    NodeImpl(Node parent, String path) {
        requireNonNull(path);
        this.parent = parent;
        this.path = defaultIfEmpty(path, SLASH);
    }

    @Override
    public Optional<Node> getParent() {
        return ofNullable(parent);
    }

    @Override
    public boolean exists() {
        return exists;
    }
}
