package net.microfalx.bootstrap.jdbc.support;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

/**
 * Represents a database query.
 *
 * A query can be created using the {@link QueryProvider} and executed to retrieve results or update data.
 */
public interface Query {

    /**
     * Creates a query instance.
     * <p>
     *
     * @param schema  the schema to be used to running the SQL statement
     * @param content the SQL content
     * @return a non-null instance
     */
    static Query create(Schema schema, String content) {
        return new QueryImpl(schema, content);
    }

    /**
     * Returns the schema where the query will be executed.
     *
     * @return a non-null instance
     */
    Schema getSchema();

    /**
     * Returns the JDBC client supporting this  query.
     *
     * @return a non-null instance
     */
    JdbcClient getClient();

    /**
     * Returns the SQL statement.
     *
     * @return the SQL statement
     */
    String getSql();

    /**
     * Executes the query and returns the number of affected rows.
     *
     * @return the number of affected rows
     * @see JdbcClient.StatementSpec#update()
     */
    int update();

    /**
     * Sets the parameters for the query.
     *
     * @param values the parameter values
     * @see JdbcClient.StatementSpec#params(Object...)
     */
    Query parameters(Object... values);

    /**
     * Sets a parameter for the query.
     *
     * @param index the index of the parameter (starting from 1)
     * @param value the value of the parameter
     * @see JdbcClient.StatementSpec#param(int, Object)
     */
    Query parameter(int index, Object value);

    /**
     * Sets a named parameter for the query.
     *
     * @param name  the name of the parameters (starting from 1)
     * @param value the value of the parameter
     * @see JdbcClient.StatementSpec#param(String, Object)
     */
    Query parameter(String name, Object value);

    /**
     * Executes the query and returns a single result.
     *
     * @param extractor the value extractor
     * @param <T>       the type of the result
     * @return the value
     * @throws org.springframework.dao.EmptyResultDataAccessException if there is no result
     */
    <T> T selectOne(ResultSetExtractor<T> extractor);

    /**
     * Executes the query and returns a single result.
     *
     * @param type the type of the result
     * @param <T>  the type of the result
     * @return the value
     * @throws org.springframework.dao.EmptyResultDataAccessException if there is no result
     */
    <T> T selectOne(Class<T> type);



    /**
     * Executes the query and returns a single result.
     * <p>
     * If no value is found, the default value is returned.
     *
     * @param type the type of the result
     * @param <T>  the type of the result
     * @return the value
     * @throws org.springframework.dao.EmptyResultDataAccessException if there is no result
     */
    <T> T selectOne(Class<T> type, T defaultValue);

    /**
     * Executes the query and returns a single integer.
     * <p>
     * If there is no result, 0 is returned.
     *
     * @return the value
     */
    int selectInt();

    /**
     * Executes the query and returns a single long.
     * <p>
     * If there is no result, 0 is returned.
     *
     * @return the value
     */
    long selectLong();

    /**
     * Executes the query and returns a collection of objects.
     *
     * @param mapper the row mapper to be used to map the result set to objects
     * @param <T>    the object type
     * @return the collection of objects
     */
    <T> List<T> selectMany(RowMapper<T> mapper);

}
