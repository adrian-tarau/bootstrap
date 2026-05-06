package net.microfalx.bootstrap.restapi.client.exception;

/**
 * A base class for all server failures.
 */
public abstract class ServerException extends ApiException {

    public ServerException(int status, ApiError error) {
        super(status, error);
    }
}
