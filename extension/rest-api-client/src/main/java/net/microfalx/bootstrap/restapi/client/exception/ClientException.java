package net.microfalx.bootstrap.restapi.client.exception;

/**
 * A base class for all client failures.
 */
public abstract class ClientException extends ApiException {

    public ClientException(int status, ApiError error) {
        super(status, error);
    }
}
