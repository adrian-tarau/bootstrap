package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing unauthorized access (HTTP 401).
 */
public class UnauthorizedException extends ClientException {

    public UnauthorizedException(int status, ApiError error) {
        super(status, error);
    }
}
