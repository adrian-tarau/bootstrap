package net.microfalx.bootstrap.dos;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;

import java.net.URI;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Identifies a request (and it's outcome) received by a service.
 */
@SuppressWarnings({"UseOfClone", "java:S2157"})
@Getter
@ToString
public class Request implements Cloneable {

    private final URI uri;
    private final String address;
    private Outcome outcome;

    /**
     * Creates a successful request.
     *
     * @param uri the URI
     * @return a non-null instance
     */
    public static Request create(URI uri, String address) {
        return new Request(uri, address, Outcome.SUCCESS);
    }

    /**
     * Creates a request with a given outcome.
     *
     * @param uri     the URI
     * @param outcome the outcome
     * @return a non-null instance
     */
    public static Request create(URI uri, String address, Outcome outcome) {
        return new Request(uri, address, outcome);
    }

    private Request(URI uri, String address, Outcome outcome) {
        requireNonNull(uri);
        requireNotEmpty(address);
        requireNonNull(outcome);
        this.address = address;
        this.uri = uri;
        this.outcome = outcome;
    }

    /**
     * Returns the client address which performed the request.
     *
     * @return a non-null instance
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the URI used to performed the request.
     *
     * @return a non-null string
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the outcome of the request.
     *
     * @return a non-null enum
     */
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * Creates a new instance and changes the outcome.
     *
     * @param outcome the new outcome
     * @return a new instance
     */
    public Request withOutcome(Outcome outcome) {
        requireNonNull(outcome);
        Request copy = copy();
        copy.outcome = outcome;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Request request)) return false;
        return Objects.equals(uri, request.uri) && outcome == request.outcome;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, outcome);
    }

    private Request copy() {
        try {
            return (Request) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Returns the outcome of a request
     */
    public enum Outcome {

        /**
         * No outcome defined.
         */
        NONE,

        /**
         * The request was processed successfully using an available end point.
         */
        SUCCESS,

        /**
         * The request was processed and resulted in a failure.
         */
        FAILURE,

        /**
         * The request could not be processed due to invalid data (usually results in a HTTP 400 or "Unknown command", etc).
         */
        VALIDATION,

        /**
         * The request could not be processed since it was malformed (syntax error, invalid parameters, etc).
         */
        INVALID,

        /**
         * The request could not be processed since the requested resource was not found (usually results in a HTTP 404).
         */
        NOT_FOUND,

        /**
         * The request could not be processed since it resulted in a security failure (authentication, authorization, etc).
         */
        SECURITY,

        /**
         * The request could not be processed since it resulted in an authentication failure.
         */
        AUTHENTICATION,

        /**
         * The request could not be processed since it resulted in a authorization failure (not authorized, permission denited, etc)
         */
        AUTHORIZATION
    }

}
