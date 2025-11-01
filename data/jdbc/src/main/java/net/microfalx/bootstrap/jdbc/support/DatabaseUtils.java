package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.bootstrap.jdbc.support.mysql.MySqlDatabase;
import net.microfalx.bootstrap.jdbc.support.vertica.VerticaDatabase;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;

public class DatabaseUtils {

    private static final String COMMENT_START = "/*";
    private static final String COMMENT_END = "*/";

    public static final String JDBC_SCHEME = "jdbc";
    public static final String MYSQL_SCHEME = "mysql";
    public static final String MARIADB_SCHEME = "mariadb";
    public static final String POSTGRES_SCHEME = "postgres";
    public static final String VERTICA_SCHEME = "vertica";

    public static final Metrics METRICS = Metrics.of("Database");

    public static final Duration AVAILABILITY_INTERVAL = ofMillis(ONE_MINUTE);
    public static final Duration PING_TIMEOUT = ofSeconds(5);
    public static final Duration CONNECT_TIMEOUT = ofSeconds(5);

    /**
     * Returns the real URI behind a JDBC URI.
     *
     * @param dataSource the data source
     * @return the URI, null if not a JDBC uri
     */
    public static URI getURI(DataSource dataSource) {
        URI uri = dataSource.getUri();
        String scheme = uri.getScheme();
        if (isEmpty(scheme) || !"jdbc".equals(scheme)
                || isEmpty(uri.getSchemeSpecificPart())) return null;
        return URI.create(uri.getSchemeSpecificPart());
    }

    /**
     * Creates a JDBC URI our of a regular URI.
     * <p>
     * If the URI is already a JDBC URI it does nothing.
     *
     * @param uri the original URI
     * @return the JDBC URI
     */
    public static URI createJdbcUri(URI uri) {
        if (uri.getScheme().equals(JDBC_SCHEME)) {
            return uri;
        } else {
            return URI.create("jdbc:" + uri.toASCIIString());
        }
    }

    /**
     * Creates a database instance based on the provided data source.
     *
     * @param dataSource the data source
     * @return a non-null instance
     */
    public static Database create(DataSource dataSource) {
        Database.Type databaseType = detectDatabaseType(dataSource);
        return switch (databaseType) {
            case MYSQL, MARIADB -> new MySqlDatabase(dataSource);
            case VERTICA -> new VerticaDatabase(dataSource);
            default -> throw new DatabaseException("Unsupported database type: " + databaseType);
        };
    }

    /**
     * Returns an enumeration representing the database type.
     *
     * @param dataSource the data source
     * @return a non-null instance
     */
    public static Database.Type detectDatabaseType(DataSource dataSource) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            if (isEmpty(databaseProductName)) {
                throw new DatabaseException("Database type cannot be detected, empty product name");
            }
            databaseProductName = databaseProductName.toLowerCase();
            if (databaseProductName.contains("mysql")) {
                return Database.Type.MYSQL;
            } else if (databaseProductName.contains("mariadb")) {
                return Database.Type.MARIADB;
            } else if (databaseProductName.contains("vertica")) {
                return Database.Type.VERTICA;
            } else {
                throw new DatabaseException("Unsupported database type: " + databaseProductName + " " + databaseProductVersion);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error detecting database type", e);
        } finally {
            IOUtils.closeQuietly(connection);
        }
    }

    public static Statement.Statistics getStatistics(StatisticalSummary statisticalSummary) {
        return new StatementImpl.StatisticsImpl(statisticalSummary);
    }

    /**
     * Normalizes the statement
     *
     * @param statement the statement
     * @return a normalized statement
     */
    public static String cleanupStatement(String statement) {
        if (statement == null) return null;
        statement = statement.trim();
        String withoutComments = cleanupComments(statement);
        String withoutVertica = cleanupVertica(withoutComments);
        return withoutVertica;
    }

    /**
     * Describes a database node.
     *
     * @param node the node
     * @return the description
     */
    public static String describe(Node node) {
        if (node == null) return StringUtils.NA_STRING;
        return node.getName() + " (" + node.getDatabase().getType() + ")";
    }

    /**
     * Describes a data source.
     *
     * @param dataSource the node
     * @return the description
     */
    public static String describe(DataSource dataSource) {
        if (dataSource == null) return StringUtils.NA_STRING;
        return dataSource.getName() + " (" + dataSource.getUri() + ")";
    }

    private static String cleanupComments(String statement) {
        int startCommentIndex = statement.indexOf(COMMENT_START);
        int endCommentIndex = statement.indexOf(COMMENT_END);
        if (startCommentIndex == 0 && endCommentIndex != -1) {
            statement = statement.substring(endCommentIndex + 2).trim();
        }
        startCommentIndex = statement.indexOf(COMMENT_START);
        endCommentIndex = statement.indexOf(COMMENT_END);
        if (startCommentIndex != -1 && endCommentIndex == statement.length() - 2) {
            statement = statement.substring(0, startCommentIndex).trim();
        }
        return statement;
    }

    private static String cleanupVertica(String statement) {
        int startIndex = statement.indexOf("STREAM");
        int endIndex = statement.indexOf(")'");
        if (startIndex != -1 && endIndex != -1) {
            String endFragment = statement.substring(endIndex + 3);
            statement = statement.substring(0, startIndex) + endFragment;
        }
        return statement;
    }
}
