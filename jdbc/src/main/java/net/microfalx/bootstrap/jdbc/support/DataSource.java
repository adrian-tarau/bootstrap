package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * An interface which wraps a JDBC data source.
 */
public interface DataSource extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Creates a new data source instance.
     *
     * @param id         the identifier of the data source
     * @param name       the friendly name of the data source
     * @param dataSource the JDBC data source
     * @return a non-null instance
     */
    static DataSource create(String id, String name, javax.sql.DataSource dataSource) {
        return new DataSourceImpl(id, name, dataSource);
    }

    /**
     * Returns the URI used to connect to the data source.
     *
     * @return a non-null instance
     */
    URI getUri();

    /**
     * Returns the hostname of the server (or proxy) supporting this data source.
     *
     * @return a non-null instance
     */
    String getHostname();

    /**
     * Returns the port of the database service (or proxy) supporting this data source.
     *
     * @return a positive integer
     */
    int getPort();

    /**
     * Returns the user name used to connect to this data source.
     *
     * @return a non-null instance
     */
    String getUserName();

    /**
     * Returns the password used to connect to this data source.
     *
     * @return a non-null instance
     */
    String getPassword();

    /**
     * Returns whether the data source represents an individual database node.
     *
     * @return {@code true} if node, {@code false} otherwise
     */
    boolean isNode();

    /**
     * Returns the JDBC data source behind this data source.
     *
     * @return a non-null instance
     */
    javax.sql.DataSource unwrap();

    /**
     * Returns a map with configuration/mappings for this data source.
     * <p>
     * Each database implementation interprets these properties as needed.
     *
     * @return a non-null instance
     */
    Map<String, String> getProperties();

    /**
     * Returns a new connection from the data source.
     *
     * @return a non-null instance
     * @throws SQLException if a connection cannot be created
     * @see javax.sql.DataSource#getConnection()
     */
    Connection getConnection() throws SQLException;

    /**
     * Creates a copy of this data source and changes the URI.
     *
     * @param uri the new URI
     * @return a new instance
     */
    DataSource withUri(URI uri);

    /**
     * Creates a copy of this data source and changes the user name.
     *
     * @param userName the user name
     * @return a new instance
     */
    DataSource withUserName(String userName);

    /**
     * Creates a copy of this data source and changes the password.
     *
     * @param password the password
     * @return a new instance
     */
    DataSource withPassword(String password);

    /**
     * Creates a copy of this data source and changes the description.
     *
     * @param description the description
     * @return a new instance
     */
    DataSource withDescription(String description);

    /**
     * Creates a copy of this data source and changes the node flag.
     *
     * @param node {@code true} if node, {@code false} otherwise
     * @return a new instance
     */
    DataSource withNode(boolean node);

    /**
     * Creates a copy of this data source and changes the properties.
     *
     * @param properties the new properties
     * @return a new instance
     */
    DataSource withProperties(Map<String, String> properties);

}
