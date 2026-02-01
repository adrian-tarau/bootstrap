package net.microfalx.bootstrap.dos;

import net.microfalx.bootstrap.core.utils.GeoLocation;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.Timestampable;

import java.time.LocalDateTime;

/**
 * Holds request counts for a specific period.
 */
public interface RequestCounts extends Identifiable<String>, Nameable, Timestampable<LocalDateTime> {

    /**
     * Return the IP address.
     *
     * @return a non-null instance
     */
    String getIp();

    /**
     * Returns the canonical host name behind the requested IP address.
     *
     * @return a non-null instance
     */
    String getCanonicalHostName();

    /**
     * Returns the throughput (requests per second).
     *
     * @return a positive integer
     */
    float getThroughput();

    /**
     * Returns the number of accesses recorded so far.
     *
     * @return a positive integer
     */
    int getAccessCount();

    /**
     * Returns the number of requests resulted in a "not found" response.
     *
     * @return a positive integer
     */
    int getNotFoundCount();

    /**
     * Returns the number of failed requests, any type.
     *
     * @return a positive integer
     */
    int getFailureCount();

    /**
     * Returns the number of invalid requests (the request could not be decoded).
     *
     * @return a positive integer
     */
    int getInvalidCount();

    /**
     * Returns the number of requests rejected due to invalid business rules (field validation, etc).
     *
     * @return a positive integer
     */
    int getValidationCount();

    /**
     * Returns the number of security-related requests (requests blocked for security reasons).
     *
     * @return a positive integer
     */
    int getSecurityCount();

    /**
     * Returns the geographical location associated with the IP address.
     *
     * @return a non-null instance
     */
    GeoLocation getLocation();

}
