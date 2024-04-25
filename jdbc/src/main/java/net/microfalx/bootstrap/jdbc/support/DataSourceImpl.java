package net.microfalx.bootstrap.jdbc.support;

import com.zaxxer.hikari.HikariDataSource;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

class DataSourceImpl implements DataSource, Cloneable {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final String id;
    private final String name;
    private String description;
    private final javax.sql.DataSource dataSource;

    private URI uri;
    private String userName;
    private String password;
    private ZoneId zoneId = ZoneId.systemDefault();
    private Map<String, String> properties = new HashMap<>();
    private Duration timeout = TIMEOUT;

    private boolean node;

    DataSourceImpl(String id, String name, javax.sql.DataSource dataSource) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        requireNonNull(dataSource);
        this.id = id;
        this.name = name;
        this.dataSource = dataSource;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public URI getUri() {
        return uri != null ? uri : URI.create("/dev/null");
    }

    @Override
    public String getHostname() {
        URI realUri = DatabaseUtils.getURI(this);
        return StringUtils.defaultIfEmpty(realUri.getHost(), "localhost");
    }

    @Override
    public int getPort() {
        URI realUri = DatabaseUtils.getURI(this);
        int port = realUri.getPort();
        return port > 0 ? port : getDefaultPort(realUri);
    }

    @Override
    public String getUserName() {
        return isNotEmpty(userName) ? userName : "anonymous";
    }

    @Override
    public String getPassword() {
        return isNotEmpty(password) ? password : StringUtils.EMPTY_STRING;
    }

    @Override
    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public Map<String, String> getProperties() {
        return unmodifiableMap(properties);
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public boolean isNode() {
        return node;
    }

    @Override
    public javax.sql.DataSource unwrap() {
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public DataSource withUri(URI uri) {
        requireNonNull(uri);
        DataSourceImpl copy = copy();
        copy.uri = uri;
        return copy;
    }

    @Override
    public DataSource withUserName(String userName) {
        requireNonNull(userName);
        DataSourceImpl copy = copy();
        copy.userName = userName;
        return copy;
    }

    @Override
    public DataSource withPassword(String password) {
        requireNonNull(password);
        DataSourceImpl copy = copy();
        copy.password = password;
        return copy;
    }

    @Override
    public DataSource withZoneId(ZoneId zoneId) {
        requireNonNull(zoneId);
        DataSourceImpl copy = copy();
        copy.zoneId = zoneId;
        return copy;
    }

    @Override
    public DataSource withDescription(String description) {
        DataSourceImpl copy = copy();
        copy.description = description;
        return copy;
    }

    @Override
    public DataSource withProperties(Map<String, String> properties) {
        requireNonNull(properties);
        DataSourceImpl copy = copy();
        copy.properties = new HashMap<>(properties);
        return copy;
    }

    @Override
    public DataSource withNode(boolean node) {
        DataSourceImpl copy = copy();
        copy.node = node;
        return copy;
    }

    protected DataSourceImpl copy() {
        try {
            return (DataSourceImpl) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DataSourceImpl.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("uri=" + uri)
                .add("userName='" + userName + "'")
                .add("dataSource=" + dataSource)
                .toString();
    }

    @Override
    public void close() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
        }
    }

    private int getDefaultPort(URI realUri) {
        String scheme = realUri.getScheme();
        if (scheme.equalsIgnoreCase(MYSQL_SCHEME) || scheme.equalsIgnoreCase(MARIADB_SCHEME)) {
            return 3306;
        } else if (scheme.equalsIgnoreCase(POSTGRES_SCHEME)) {
            return 5432;
        } else if (scheme.equalsIgnoreCase(VERTICA_SCHEME)) {
            return 5433;
        } else {
            throw new IllegalStateException("Unknown JDBC driver URL: " + realUri);
        }

    }
}
