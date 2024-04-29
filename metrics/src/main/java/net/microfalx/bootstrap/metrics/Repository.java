package net.microfalx.bootstrap.metrics;

/**
 * A repository for metrics.
 * <p>
 * Each repository can provide data for a given {@link Query#getType() type}.
 */
public interface Repository {

    /**
     * Returns whether a repository is available.
     *
     * @return <code>true</code> if available, <code>false</code> otherwise
     */
    boolean isAvailable();

    /**
     * Returns whether the repository supports a given query.
     *
     * @param query the query
     * @return {@code true} if it supports the query, {@code false} otherwise
     */
    boolean supports(Query query);

    /**
     * Returns the last error received from the repository.
     *
     * @return the error, null if there is no error
     */
    String getLastError();

    /**
     * Performs a query.
     *
     * @param query the query
     * @return the result
     */
    Result query(Query query);


}
