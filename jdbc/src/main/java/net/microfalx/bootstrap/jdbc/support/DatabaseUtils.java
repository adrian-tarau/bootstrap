package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;

import java.net.URI;

public class DatabaseUtils {

    public static final String JDBC_SCHEME = "jdbc";
    public static final String MYSQL_SCHEME = "mysql";
    public static final String MARIADB_SCHEME = "mariadb";
    public static final String POSTGRES_SCHEME = "postgres";
    public static final String VERTICA_SCHEME = "vertica";

    static final Metrics DATABASE = Metrics.of("Database");

    /**
     * Returns the real URI behind a JDBC URI.
     *
     * @param dataSource the data source
     * @return the URI, null if not a JDBC uri
     */
    public static URI getURI(DataSource dataSource) {
        URI uri = dataSource.getUri();
        String scheme = uri.getScheme();
        if (StringUtils.isEmpty(scheme) || !"jdbc".equals(scheme)
                || StringUtils.isEmpty(uri.getSchemeSpecificPart())) return null;
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
}
