package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing permission denited (HTTP 403).
 */
public class ForbiddenException extends ClientException {

    public ForbiddenException(int status, ApiError error) {
        super(status, error);
    }
}
