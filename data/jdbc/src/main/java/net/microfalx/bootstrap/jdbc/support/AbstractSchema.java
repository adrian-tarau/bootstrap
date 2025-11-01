package net.microfalx.bootstrap.jdbc.support;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * Base class for all schema implementations.
 */
@Slf4j
public abstract class AbstractSchema implements Schema {

    public static final Metrics METRICS = DatabaseUtils.METRICS.withGroup("Schema");

    private final Database database;
    private volatile String name;

    private volatile Set<String> tableNames;
    private final Map<String, Table<?>> tables = new ConcurrentHashMap<>();
    private volatile Set<String> indexNames;
    private final Map<String, Index<?>> indexes = new ConcurrentHashMap<>();

    private final Map<String, Integer> JDBC_TYPE_CACHE = new HashMap<>();

    public AbstractSchema(Database database) {
        requireNonNull(database);
        this.database = database;
    }

    @Override
    public final String getId() {
        return database.getId();
    }

    @Override
    public final Database getDatabase() {
        return database;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = database.getSchemaName();
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractSchema that)) return false;
        return Objects.equals(database, that.database);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(database);
    }

    @Override
    public final Set<String> getTableNames() {
        if (ObjectUtils.isEmpty(tableNames)) {
            tableNames = METRICS.time("Get Table Names", this::doGetTableNames);
        }
        return unmodifiableSet(tableNames);
    }

    @Override
    public final Set<String> getIndexNames() {
        if (ObjectUtils.isEmpty(indexNames)) {
            indexNames = METRICS.time("Get Index Names", this::doGetIndexNames);
        }
        return unmodifiableSet(indexNames);
    }

    @Override
    public final Table<?> getTable(String name) {
        return tables.computeIfAbsent(name, s -> METRICS.time("Get Table", () -> doGetTable(name)));
    }

    @Override
    public final Index<?> getIndex(String name) {
        return indexes.computeIfAbsent(name, s -> METRICS.time("Get Index", () -> doGetIndex(name)));
    }

    @Override
    public Query getQuery(String path) {
        Resource resource = getResource("queries" + addStartSlash(path));
        try {
            return Query.create(this, resource.loadAsString());
        } catch (IOException e) {
            throw new ScriptException("A SQL file could not be loaded from " + path + "'");
        }
    }

    @Override
    public Script getScript(String path) {
        return createScript(getResource(path));
    }

    public Resource getResource(String path) {
        requireNotEmpty(path);
        path = addEndSlash("sql/" + getDatabase().getType().name().toLowerCase()) + removeStartSlash(path);
        Resource resource = ClassPathResource.file(path);
        try {
            if (resource.exists()) {
                return resource;
            } else {
                throw new ScriptException("A SQL file does not exist at '" + path + "' for schema " + getName());
            }
        } catch (IOException e) {
            throw new ScriptException("A SQL file could not be loaded from " + path + "' for schema " + getName(), e);
        }
    }

    public int getJdbcType(String typeName) {
        return JDBC_TYPE_CACHE.computeIfAbsent(typeName, tn -> {
            try {
                return Types.class.getField(tn.toUpperCase()).getInt(null);
            } catch (Exception e) {
                LOGGER.warn("Unknown JDBC type: {}", typeName);
                return Types.OTHER;
            }
        });
    }

    public void registerJdbcType(String typeName, int jdbcType) {
        requireNonNull(typeName);
        JDBC_TYPE_CACHE.put(typeName, jdbcType);
    }

    @Override
    public void clearCache() {
        indexNames = null;
        tableNames = null;
        tables.clear();
        indexes.clear();
    }

    protected abstract Set<String> doGetTableNames();

    protected abstract Table<?> doGetTable(String name);

    protected abstract Set<String> doGetIndexNames();

    protected abstract Index<?> doGetIndex(String name);

    protected abstract Script createScript(Resource resource);

}
