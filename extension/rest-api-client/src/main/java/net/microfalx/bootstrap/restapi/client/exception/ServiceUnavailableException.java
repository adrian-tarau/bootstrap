package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing a service issue (HTTP 503).
 */
public class ServiceUnavailableException extends ServerException {

    public ServiceUnavailableException(int status, ApiError error) {
        super(status, error);
    }
}
