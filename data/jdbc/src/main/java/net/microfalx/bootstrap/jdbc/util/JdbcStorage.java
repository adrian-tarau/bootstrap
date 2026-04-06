package net.microfalx.bootstrap.jdbc.util;

import net.microfalx.bootstrap.jdbc.support.Query;
import net.microfalx.bootstrap.jdbc.support.QueryProvider;
import net.microfalx.bootstrap.registry.AbstractStorage;
import net.microfalx.bootstrap.registry.Node;
import net.microfalx.bootstrap.registry.RegistryUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.bootstrap.registry.RegistryUtils.getNaturalId;
import static net.microfalx.bootstrap.registry.RegistryUtils.normalizePath;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class JdbcStorage extends AbstractStorage {

    private final Map<String, Long> nodeIds = new ConcurrentHashMap<>();
    private final QueryProvider queryProvider;

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public JdbcStorage(QueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    @Override
    public Collection<Node> getChildren(String path, boolean recursive) {
        String normalizedPath = normalizePath(path);
        Query query;
        if (recursive) {
            query = queryProvider.withResource("registry.get_children.sql").parameter(1, normalizedPath + "/%");
        } else {
            query = queryProvider.withResource("registry.get_direct_children.sql").parameter(1, getParentId(normalizedPath));
        }
        try {
            return query.selectMany((rs, rowNum) -> {
                AbstractStorage.StorageNode node = mapToStorageNode(rs);
                node.setExists(true);
                node.setLeaf(!hasChildren(node.getPath()));
                return node;
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Optional<Node> getNode(String path) {
        String normalizedPath = normalizePath(path);
        Query query = queryProvider.withResource("registry.get_node.sql")
                .parameter(1, RegistryUtils.getNaturalId(normalizedPath));
        try {
            AbstractStorage.StorageNode node = query.selectOne((rs, rowNum) -> mapToStorageNode(rs));
            if (node != null) {
                node.setExists(true);
                node.setLeaf(!hasChildren(path));
            }
            return Optional.ofNullable(node);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String path) {
        String normalizedPath = normalizePath(path);
        return getId(normalizedPath) != null;
    }

    @Override
    public byte[] get(String path) {
        String normalizedPath = normalizePath(path);
        Long id = getId(normalizedPath);
        if (id == null) return EMPTY_BYTE_ARRAY;
        Query query = queryProvider.withResource("registry.get_data.sql");
        query.parameter(1, id);
        try {
            return query.selectOne(String.class).getBytes();
        } catch (IncorrectResultSizeDataAccessException e) {
            return EMPTY_BYTE_ARRAY;
        }
    }

    @Override
    public void put(String path, byte[] data) {
        put(path, data, 0);
    }

    @Override
    public void put(String path, byte[] data, int version) {
        String normalizedPath = normalizePath(path);
        Long parentId = getParentId(normalizedPath);
        LocalDateTime now = LocalDateTime.now();
        Query nodeQuery = queryProvider.withResource("registry.put_node.sql");
        nodeQuery.parameters(parentId, getNaturalId(normalizedPath), normalizedPath, version, 1, now, now);
        nodeQuery.update();
        Long id = getId(normalizedPath);
        Query dataQuery = queryProvider.withResource("registry.put_data.sql");
        dataQuery.parameters(id, data);
        dataQuery.update();
    }

    @Override
    public void remove(String path) {
        String normalizedPath = normalizePath(path);
        Long id = getId(normalizedPath);
        if (id != null) {
            queryProvider.withResource("registry.remove_data.sql").parameter(1, id).update();
            queryProvider.withResource("registry.remove_node.sql").parameter(1, id).update();
            nodeIds.remove(getNaturalId(normalizedPath));
        }
    }

    private AbstractStorage.StorageNode mapToStorageNode(ResultSet rs) throws SQLException {
        AbstractStorage.StorageNode node = new AbstractStorage.StorageNode(rs.getLong("id"),
                rs.getString("natural_id"), rs.getString("path"));
        node.setUpdateCount(rs.getInt("update_count"))
                .setVersion(rs.getInt("version"));
        node.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
                .setUpdatedAt(rs.getTimestamp("modified_at").toLocalDateTime());
        return node;
    }

    private boolean hasChildren(String path) {
        Long parentId = getParentId(path);
        if (parentId != null) {
            return queryProvider.withResource("registry.count_children.sql")
                    .parameter(1, parentId)
                    .selectInt() > 0;
        } else {
            return false;
        }
    }

    private Long getParentId(String path) {
        String parentPath = RegistryUtils.getParent(path);
        return getId(parentPath);
    }

    private Long getId(String path) {
        String naturalId = RegistryUtils.getNaturalId(path);
        Long id = nodeIds.get(naturalId);
        if (id == null) {
            try {
                id = queryProvider.withResource("registry.get_id.sql")
                        .parameter(1, naturalId)
                        .selectOne(Long.class);
            } catch (IncorrectResultSizeDataAccessException e) {
                // ignore
            }
        }
        if (id != null) nodeIds.put(naturalId, id);
        return id;
    }
}
