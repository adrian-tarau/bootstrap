package net.microfalx.bootstrap.registry;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Base class for all registry implementations.
 */
public abstract class AbstractStorage implements Storage {

    @Data
    protected static class StorageNode implements Node {

        private final long id;
        private final String naturalId;
        private final String path;
        private boolean leaf;
        @Getter(AccessLevel.NONE)
        private boolean exists;
        private int updateCount;
        private int version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Override
        public Optional<Node> getParent() {
            return Optional.empty();
        }

        @Override
        public boolean exists() {
            return exists;
        }
    }
}
