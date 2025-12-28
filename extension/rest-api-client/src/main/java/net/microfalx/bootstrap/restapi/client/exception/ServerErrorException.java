package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing server-side errors (HTTP 5xx).
 */
public class ServerErrorException extends ApiException {

    public ServerErrorException(int status, ApiError error) {
        super(status, error);
    }

    public ServerErrorException(int status, ApiError error, Throwable cause) {
        super(status, error);
        initCause(cause);
    }
}
