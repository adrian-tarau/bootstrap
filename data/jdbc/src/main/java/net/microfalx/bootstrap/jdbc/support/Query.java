package net.microfalx.bootstrap.jdbc.support;

/**
 * Represents a database query.
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
     * Returns the SQL statement.
     *
     * @return the SQL statement
     */
    String getSql();

    /**
     * Executes the query and returns the number of affected rows.
     *
     * @return the number of affected rows
     */
    int update();

    /**
     * Sets the parameters for the query.
     *
     * @param values the parameter values
     */
    Query parameters(Object... values);

    /**
     * Sets a parameter for the query.
     *
     * @param index the index of the parameter (starting from 1)
     * @param value the value of the parameter
     */
    Query parameter(int index, Object value);

    /**
     * Sets a named parameter for the query.
     *
     * @param name  the name of the parameters (starting from 1)
     * @param value the value of the parameter
     */
    Query parameter(String name, Object value);

    /**
     * Executes the query and returns a single result.
     *
     * @param type the type of the result
     * @param <T>  the type of the result
     * @return the value
     * @throws org.springframework.dao.EmptyResultDataAccessException if there is no result
     */
    <T> T selectOne(Class<?> type);

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
    <T> T selectOne(Class<?> type, T defaultValue);


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

}
