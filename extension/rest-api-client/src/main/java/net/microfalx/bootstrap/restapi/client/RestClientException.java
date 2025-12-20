package net.microfalx.bootstrap.restapi.client;

/**
 * Base exception for REST client errors.
 */
public class RestClientException extends RuntimeException {

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
